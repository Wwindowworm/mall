package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BizException;
import com.mall.common.ErrorCode;
import com.mall.entity.SeckillActivity;
import com.mall.entity.SeckillOrder;
import com.mall.mapper.SeckillActivityMapper;
import com.mall.mapper.SeckillOrderMapper;
import com.mall.service.SeckillService;
import com.mall.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final Logger log = LoggerFactory.getLogger(SeckillServiceImpl.class);

    @Autowired
    private SeckillActivityMapper activityMapper;
    @Autowired
    private SeckillOrderMapper orderMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    private static final String SECKILL_USER_PREFIX = "seckill:user:";

    private static final String SECKILL_LUA_SCRIPT =
        "local stock = redis.call('GET', KEYS[1])\n" +
        "if stock == false then return -1 end\n" +
        "stock = tonumber(stock)\n" +
        "if stock <= 0 then return 0 end\n" +
        "redis.call('DECR', KEYS[1])\n" +
        "return 1";

    @Override
    public List<SeckillActivity> listSeckillActivities() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeckillActivity::getStatus, 1)
               .le(SeckillActivity::getStartTime, now)
               .ge(SeckillActivity::getEndTime, now)
               .orderByDesc(SeckillActivity::getCreateTime);
        return activityMapper.selectList(wrapper);
    }

    @Override
    public SeckillActivity getSeckillDetail(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getDeleted() == 1) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return activity;
    }

    @Override
    @Transactional
    public String seckill(Long userId, Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getDeleted() == 1) {
            throw new BizException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) {
            throw new BizException(ErrorCode.SECKILL_NOT_START);
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BizException(ErrorCode.SECKILL_ALREADY_END);
        }

        String userKey = SECKILL_USER_PREFIX + activityId + ":" + userId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(userKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isNew)) {
            throw new BizException(ErrorCode.SECKILL_USER_LIMIT);
        }

        String stockKey = SECKILL_STOCK_PREFIX + activityId;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(SECKILL_LUA_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(stockKey));

        if (result == null || result == -1) {
            redisTemplate.opsForValue().set(stockKey, String.valueOf(activity.getRemainStock()));
            result = redisTemplate.execute(script, Collections.singletonList(stockKey));
        }

        if (result == null || result == 0) {
            throw new BizException(ErrorCode.SECKILL_STOCK_ZERO);
        }

        String lockKey = "seckill:lock:" + activityId;
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, userId.toString(), 5, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(lockAcquired)) {
            throw new BizException(500, "系统繁忙，请稍后重试");
        }
        try {
            SeckillActivity updateActivity = activityMapper.selectById(activityId);
            if (updateActivity.getRemainStock() <= 0) {
                redisTemplate.opsForValue().set(stockKey, "0");
                throw new BizException(ErrorCode.SECKILL_STOCK_ZERO);
            }
            updateActivity.setRemainStock(updateActivity.getRemainStock() - 1);
            activityMapper.updateById(updateActivity);

            SeckillOrder order = new SeckillOrder();
            order.setOrderNo(IdUtil.generateOrderNo());
            order.setUserId(userId);
            order.setActivityId(activityId);
            order.setProductId(activity.getProductId());
            order.setSeckillPrice(activity.getSeckillPrice());
            order.setStatus(1);
            orderMapper.insert(order);

            log.info("秒杀成功，userId={}, activityId={}, orderNo={}", userId, activityId, order.getOrderNo());
            return order.getOrderNo();
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}

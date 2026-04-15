package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BizException;
import com.mall.common.ErrorCode;
import com.mall.entity.SignRecord;
import com.mall.entity.User;
import com.mall.mapper.SignRecordMapper;
import com.mall.mapper.UserMapper;
import com.mall.service.SignService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SignServiceImpl implements SignService {

    private static final Logger log = LoggerFactory.getLogger(SignServiceImpl.class);

    @Autowired
    private SignRecordMapper signRecordMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public SignRecord sign(Long userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<SignRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignRecord::getUserId, userId)
               .eq(SignRecord::getSignDate, today);
        long count = signRecordMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ErrorCode.ALREADY_SIGN_IN);
        }

        SignRecord record = new SignRecord();
        record.setUserId(userId);
        record.setSignDate(today);
        record.setRewardPoints(1);
        signRecordMapper.insert(record);

        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setTotalSignDays(user.getTotalSignDays() + 1);
            user.setLastSignTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        log.info("签到成功，userId={}", userId);
        return record;
    }

    @Override
    public List<SignRecord> getSignCalendar(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        LambdaQueryWrapper<SignRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SignRecord::getUserId, userId)
               .ge(SignRecord::getSignDate, start)
               .lt(SignRecord::getSignDate, end);
        return signRecordMapper.selectList(wrapper);
    }
}

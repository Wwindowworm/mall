package com.mall.controller;

import com.mall.common.Result;
import com.mall.entity.SeckillActivity;
import com.mall.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @GetMapping("/list")
    public Result<List<SeckillActivity>> listActivities() {
        return Result.ok(seckillService.listSeckillActivities());
    }

    @GetMapping("/detail/{id}")
    public Result<SeckillActivity> getDetail(@PathVariable Long id) {
        return Result.ok(seckillService.getSeckillDetail(id));
    }

    @PostMapping("/do")
    public Result<String> doSeckill(
            @RequestAttribute("userId") Long userId,
            @RequestBody SeckillDTO dto
    ) {
        String orderNo = seckillService.seckill(userId, dto.getActivityId());
        return Result.ok("秒杀成功，订单号：" + orderNo, orderNo);
    }

    public static class SeckillDTO {
        private Long activityId;
        public Long getActivityId() { return activityId; }
        public void setActivityId(Long activityId) { this.activityId = activityId; }
    }
}

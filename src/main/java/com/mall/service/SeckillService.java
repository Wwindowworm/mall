package com.mall.service;

import com.mall.entity.SeckillActivity;

import java.util.List;

public interface SeckillService {
    List<SeckillActivity> listSeckillActivities();
    SeckillActivity getSeckillDetail(Long activityId);
    String seckill(Long userId, Long activityId);
}

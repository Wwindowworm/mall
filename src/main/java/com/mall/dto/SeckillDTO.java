package com.mall.dto;

import jakarta.validation.constraints.NotNull;

public class SeckillDTO {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
}

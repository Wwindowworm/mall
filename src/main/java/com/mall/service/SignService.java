package com.mall.service;

import com.mall.entity.SignRecord;

import java.util.List;

public interface SignService {
    SignRecord sign(Long userId);
    List<SignRecord> getSignCalendar(Long userId, int year, int month);
}

package com.mall.service;

import com.mall.entity.User;
import java.util.Map;

public interface UserService {
    void sendCode(String email, String clientIp);
    Map<String, Object> login(String email, String code);
    User getUserById(Long userId);
}

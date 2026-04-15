package com.mall.controller;

import com.mall.config.JwtConfig;
import com.mall.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 调试接口：用于生成有效的测试token，并验证token解析是否正常
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private JwtConfig jwtConfig;

    /** 生成测试token（userId=5，用于调试） */
    @GetMapping("/gen-token")
    public Result<?> genToken() {
        try {
            String token = jwtConfig.generateToken(5L, "2121666063@qq.com");
            return Result.ok(token);
        } catch (Exception e) {
            return Result.fail("生成失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    /** 解析当前请求的Authorization头，返回userId（用于诊断鉴权问题） */
    @GetMapping("/check-token")
    public Result<?> checkToken(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || auth.isEmpty()) {
            return Result.fail("无Authorization头");
        }
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
        try {
            Long userId = jwtConfig.getUserId(token);
            boolean expired = jwtConfig.isExpired(token);
            return Result.ok(Map.of("userId", userId, "expired", expired, "raw", token.substring(0, 30) + "..."));
        } catch (Exception e) {
            return Result.fail("解析失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    /** 测试鉴权接口（必须带token才返回200） */
    @GetMapping("/test-auth")
    public Result<?> testAuth() {
        return Result.ok("鉴权通过！");
    }
}

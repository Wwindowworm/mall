package com.mall.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);
    public static final String USER_ID = "userId";

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        String token = null;
        // 优先从 Authorization: Bearer <token> 获取
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (authHeader != null && !authHeader.isEmpty()) {
            // 也支持 Authorization: <token>（无 Bearer 前缀）
            token = authHeader;
        }
        // 其次从 Cookie 中获取
        if (token == null || token.isEmpty()) {
            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (jakarta.servlet.http.Cookie c : cookies) {
                    if ("token".equals(c.getName())) {
                        token = c.getValue();
                        break;
                    }
                }
            }
        }
        if (token != null && !token.isEmpty()) {
            try {
                boolean expired = jwtConfig.isExpired(token);
                if (!expired) {
                    Long userId = jwtConfig.getUserId(token);
                    request.setAttribute(USER_ID, userId);
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return true;
    }
}

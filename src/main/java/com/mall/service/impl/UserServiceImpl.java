package com.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BizException;
import com.mall.common.ErrorCode;
import com.mall.config.JwtConfig;
import com.mall.entity.User;
import com.mall.mapper.UserMapper;
import com.mall.service.UserService;
import com.mall.util.EmailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private EmailUtil emailUtil;

    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String EMAIL_IP_PREFIX = "email:ip:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int IP_LIMIT_COUNT = 5;
    private static final int IP_LIMIT_WINDOW = 60;

    @Override
    public void sendCode(String email, String clientIp) {
        String ipKey = EMAIL_IP_PREFIX + clientIp;
        Long ipCount = redisTemplate.opsForValue().increment(ipKey);
        if (ipCount != null && ipCount == 1) {
            redisTemplate.expire(ipKey, IP_LIMIT_WINDOW, TimeUnit.SECONDS);
        }
        if (ipCount != null && ipCount > IP_LIMIT_COUNT) {
            throw new BizException(ErrorCode.SMS_IP_LIMIT);
        }

        String code = String.format("%06d", new Random().nextInt(1000000));
        String codeKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        boolean ok = emailUtil.sendVerificationCode(email, code);
        if (!ok) {
            log.warn("邮件发送失败，降级 Mock，email={}", email);
        }
        log.info("发送验证码，email={}，code={}", email, code);
    }

    @Override
    public Map<String, Object> login(String email, String code) {
        String codeKey = EMAIL_CODE_PREFIX + email;
        String cachedCode = redisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || !cachedCode.equals(code)) {
            throw new BizException(ErrorCode.SMS_CODE_ERROR);
        }
        redisTemplate.delete(codeKey);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setNickname("用户" + email.substring(0, email.indexOf("@")));
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
            log.info("新用户注册，email={}", email);
        } else {
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
        }

        String token = jwtConfig.generateToken(user.getId(), user.getEmail());
        return Map.of(
                "token", token,
                "userId", user.getId(),
                "nickname", user.getNickname(),
                "email", user.getEmail()
        );
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }
}

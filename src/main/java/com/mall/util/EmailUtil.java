package com.mall.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${email.from-name:优选网购}")
    private String fromName;

    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromEmail + ">");
            message.setTo(toEmail);
            message.setSubject("【优选网购】您的验证码");
            message.setText(buildEmailContent(code));
            mailSender.send(message);
            log.info("邮件发送成功，to={}, code={}", toEmail, code);
            return true;
        } catch (Exception e) {
            log.error("邮件发送失败，to={}, error={}", toEmail, e.getMessage());
            return false;
        }
    }

    private String buildEmailContent(String code) {
        return "尊敬的用户您好！\n\n" +
               "您的验证码为：" + code + "\n\n" +
               "有效期 5 分钟，请勿告知他人。\n\n" +
               "如非本人操作，请忽略此邮件。\n\n" +
               "—— 优选网购";
    }
}

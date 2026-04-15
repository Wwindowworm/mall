package com.mall.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class SmsUtil {

    private static final Logger log = LoggerFactory.getLogger(SmsUtil.class);
    private static final String DOMAIN = "dysmsapi.aliyuncs.com";
    private static final String VERSION = "2017-05-25";
    private static final String ACTION = "SendSms";
    private static final String FORMAT = "JSON";
    private static final String REGION_ID = "cn-hangzhou";
    private static final String SIGN_METHOD = "HMAC-SHA1";
    private static final String SIGN_VERSION = "1.0";

    @Autowired
    private RestTemplate restTemplate;

    @Value("${sms.access-key-id:}")
    private String accessKeyId;

    @Value("${sms.access-key-secret:}")
    private String accessKeySecret;

    @Value("${sms.sign-name:}")
    private String signName;

    @Value("${sms.template-code:}")
    private String templateCode;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public boolean sendCode(String phone, String code) {
        if (accessKeyId == null || accessKeyId.isBlank()
                || accessKeySecret == null || accessKeySecret.isBlank()
                || templateCode == null || templateCode.isBlank()) {
            return mockSend(phone, code);
        }
        try {
            String response = doSendSms(phone, code);
            return parseResponse(response, phone, code);
        } catch (Exception e) {
            log.error("短信发送异常，降级 Mock：{}", e.getMessage());
            return mockSend(phone, code);
        }
    }

    private String doSendSms(String phone, String code) throws Exception {
        Map<String, String> sortedParams = new TreeMap<>();
        sortedParams.put("AccessKeyId", accessKeyId);
        sortedParams.put("Action", ACTION);
        sortedParams.put("Format", FORMAT);
        sortedParams.put("PhoneNumbers", phone);
        sortedParams.put("RegionId", REGION_ID);
        sortedParams.put("SignName", signName);
        sortedParams.put("SignatureMethod", SIGN_METHOD);
        sortedParams.put("SignatureNonce", UUID.randomUUID().toString());
        sortedParams.put("SignatureVersion", SIGN_VERSION);
        sortedParams.put("TemplateCode", templateCode);
        sortedParams.put("TemplateParam", "{\"code\":\"" + code + "\"}");
        sortedParams.put("Timestamp", formatTimestamp());
        sortedParams.put("Version", VERSION);

        String canonicalQS = buildCanonicalQueryString(sortedParams);
        log.debug("规范化查询字符串: {}", canonicalQS);

        String stringToSign = "POST&" + percentEncode("/") + "&" + percentEncode(canonicalQS);
        log.debug("待签名字符串: {}", stringToSign);

        String signature = calculateSignature(stringToSign);
        sortedParams.put("Signature", signature);
        log.debug("签名: {}", signature);

        String url = "https://" + DOMAIN;
        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        sortedParams.forEach(formParams::add);

        log.info("阿里云短信请求，phone={}, signName={}, templateCode={}", phone, signName, templateCode);
        return restTemplate.postForObject(url, formParams, String.class);
    }

    private String buildCanonicalQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) sb.append("&");
            first = false;
            sb.append(percentEncode(entry.getKey()))
              .append("=")
              .append(percentEncode(entry.getValue()));
        }
        return sb.toString();
    }

    private String percentEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8")
                    .replace("*", "%2A")
                    .replace("+", "%20");
        } catch (Exception e) {
            return value;
        }
    }

    private String calculateSignature(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(
                (accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    private String formatTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    private boolean parseResponse(String response, String phone, String code) {
        try {
            JsonNode node = objectMapper.readTree(response);
            String bizCode = node.has("Code") ? node.get("Code").asText() : "";
            if ("OK".equals(bizCode)) {
                String bizId = node.has("BizId") ? node.get("BizId").asText() : "";
                log.info("短信发送成功，phone={}, code={}, bizId={}", phone, code, bizId);
                return true;
            } else {
                String message = node.has("Message") ? node.get("Message").asText() : "未知";
                String requestId = node.has("RequestId") ? node.get("RequestId").asText() : "";
                log.error("短信发送失败，Code={}, Message={}, RequestId={}", bizCode, message, requestId);
                return false;
            }
        } catch (Exception e) {
            log.warn("解析响应失败：{}", response);
            return false;
        }
    }

    private boolean mockSend(String phone, String code) {
        System.out.println("【Mock SMS】发送给 " + phone + "，验证码：" + code);
        log.info("【Mock SMS】发送给 {}，验证码：{}", phone, code);
        return true;
    }
}

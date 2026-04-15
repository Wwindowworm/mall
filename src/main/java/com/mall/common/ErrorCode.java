package com.mall.common;

public enum ErrorCode {
    // 系统
    SYSTEM_ERROR(500, "系统异常，请稍后重试"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),

    // 认证
    LOGIN_FAILED(1001, "登录失败，账号或密码错误"),
    SMS_CODE_ERROR(1002, "验证码错误或已过期"),
    SMS_CODE_SEND_TOO_FAST(1003, "验证码发送太频繁，请稍后重试"),
    SMS_IP_LIMIT(1004, "请求过于频繁，请稍后重试"),
    USER_NOT_EXIST(1005, "用户不存在"),

    // 商品
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_OFF_SHELF(2002, "商品已下架"),
    PRODUCT_STOCK_NOT_ENOUGH(2003, "库存不足"),

    // 秒杀
    SECKILL_NOT_START(3001, "秒杀尚未开始"),
    SECKILL_ALREADY_END(3002, "秒杀已结束"),
    SECKILL_USER_LIMIT(3003, "您已购买过该秒杀商品"),
    SECKILL_STOCK_ZERO(3004, "库存已售罄"),
    SECKILL_ORDER_FAIL(3005, "秒杀下单失败"),

    // 签到
    ALREADY_SIGN_IN(4001, "今日已签到"),
    SIGN_IN_FAIL(4002, "签到失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}

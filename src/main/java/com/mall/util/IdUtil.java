package com.mall.util;

public class IdUtil {
    public static String generateOrderNo() {
        return cn.hutool.core.util.IdUtil.getSnowflakeNextIdStr();
    }
}

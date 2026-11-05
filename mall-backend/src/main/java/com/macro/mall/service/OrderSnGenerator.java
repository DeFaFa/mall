package com.macro.mall.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderSnGenerator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 订单号：日期时间 + 6 位随机数 + 毫秒后两位
     */
    public String next() {
        LocalDateTime now = LocalDateTime.now();
        String millis = String.format("%03d", now.getNano() / 1_000_000);
        String msTail = millis.substring(millis.length() - 2);
        int random = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return FORMATTER.format(now) + random + msTail;
    }
}

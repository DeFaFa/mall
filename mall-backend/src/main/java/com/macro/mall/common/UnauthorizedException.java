package com.macro.mall.common;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super("请先登录");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}

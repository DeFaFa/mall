package com.macro.mall.common;

/**
 * 统一异常处理：业务异常走 900，参数校验走 400，鉴权走 401/403。
 */
@org.springframework.web.bind.annotation.RestControllerAdvice
public class GlobalExceptionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(BizException.class)
    public CommonResult<Void> handleBiz(BizException e) {
        return CommonResult.failed(e.getCode(), e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public CommonResult<Void> handleValid(org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .findFirst().orElse("参数校验失败");
        return CommonResult.failed(400, message);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public CommonResult<Void> handleMissingParam(org.springframework.web.bind.MissingServletRequestParameterException e) {
        return CommonResult.failed(400, "缺少参数：" + e.getParameterName());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UnauthorizedException.class)
    public CommonResult<Void> handleUnauthorized(UnauthorizedException e) {
        return CommonResult.failed(401, e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ForbiddenException.class)
    public CommonResult<Void> handleForbidden(ForbiddenException e) {
        return CommonResult.failed(403, e.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public CommonResult<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return CommonResult.failed(500, "系统繁忙，请稍后重试");
    }
}

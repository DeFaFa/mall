package com.macro.mall.controller.portal;

import com.macro.mall.auth.TokenUtils;
import com.macro.mall.auth.TokenService;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.LoginParam;
import com.macro.mall.dto.RegisterParam;
import com.macro.mall.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/portal/auth")
@RequiredArgsConstructor
public class PortalAuthController {
    private final MemberService memberService;
    private final TokenService tokenService;
    private final StringRedisTemplate redis;

    @PostMapping("/register")
    public CommonResult<Long> register(@RequestBody RegisterParam param) {
        return CommonResult.success(memberService.register(param));
    }

    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody LoginParam param) {
        return CommonResult.success(memberService.login(param.getUsername(), param.getPassword()));
    }

    @PostMapping("/logout")
    public CommonResult<Void> logout(HttpServletRequest request) {
        memberService.logout(TokenUtils.resolve(request));
        return CommonResult.success();
    }

    /**
     * 演示环境短信验证码：写入 Redis 并直接返回，真实环境由短信网关下发。
     */
    @GetMapping("/smsCode")
    public CommonResult<String> smsCode(@RequestParam String phone) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
        redis.opsForValue().set("mall:sms:code:" + phone, code, Duration.ofMinutes(5));
        return CommonResult.success(code);
    }
}

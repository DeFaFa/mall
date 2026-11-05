package com.macro.mall.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 基于 Redis 的双身份 Token：后台 admin 与前台 member 使用不同前缀，互不串用。
 */
@Slf4j
@Service
public class TokenService {
    private static final String ADMIN_PREFIX = "mall:admin:token:";
    private static final String MEMBER_PREFIX = "mall:member:token:";

    private final StringRedisTemplate redis;
    private final Duration timeout;

    public TokenService(StringRedisTemplate redis,
                        @Value("${mall.token-timeout-hours:12}") long timeoutHours) {
        this.redis = redis;
        this.timeout = Duration.ofHours(timeoutHours);
    }

    public String createAdminToken(Long adminId) {
        return create(ADMIN_PREFIX, adminId);
    }

    public String createMemberToken(Long memberId) {
        return create(MEMBER_PREFIX, memberId);
    }

    public Long getAdminId(String token) {
        return read(ADMIN_PREFIX, token);
    }

    public Long getMemberId(String token) {
        return read(MEMBER_PREFIX, token);
    }

    public void removeAdminToken(String token) {
        redis.delete(ADMIN_PREFIX + token);
    }

    public void removeMemberToken(String token) {
        redis.delete(MEMBER_PREFIX + token);
    }

    private String create(String prefix, Long id) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(prefix + token, String.valueOf(id), timeout);
        return token;
    }

    private Long read(String prefix, String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String value = redis.opsForValue().get(prefix + token);
        return value == null ? null : Long.valueOf(value);
    }
}

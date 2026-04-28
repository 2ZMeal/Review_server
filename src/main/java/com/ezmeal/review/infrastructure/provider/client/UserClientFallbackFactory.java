package com.ezmeal.review.infrastructure.provider.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        return userId -> {
            log.error("User Service 호출 실패(Fallback) - userId: {}, 원인: {}", userId, cause.getMessage());
            throw new RuntimeException("사용자 정보를 불러올 수 없습니다. 외부 서버 장애");
        };
    }
}

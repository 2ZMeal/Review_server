package com.ezmeal.review.infrastructure.provider.client.dto;

import java.time.LocalDateTime;
import java.util.UUID;

// 회원 서버에서 사용자 id로 조회하여 사용자 정보를 가져올 때 받는 응답 형식
public record UserResponse(
        UUID userId,
        String name,
        String nickname,
        String email,
        String role,
        String status,
        LocalDateTime lastLoginAt,
        LocalDateTime signupAt
) {}

package com.ezmeal.review.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.common.enums.Role;
import com.ezmeal.review.application.dto.command.ReviewDeleteCommand;
import java.util.UUID;

public record ReviewDeleteMessage(
        UUID reviewId
) {
    // Message와 카프카 헤더에서 추출한 인증 정보를 조합해 비즈니스 Command로 변환
    public ReviewDeleteCommand toCommand(String userId, Role role) {
        return new ReviewDeleteCommand(
                this.reviewId,
                userId,
                role
        );
    }
}

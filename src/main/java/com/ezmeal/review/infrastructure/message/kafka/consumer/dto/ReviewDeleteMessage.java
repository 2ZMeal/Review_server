package com.ezmeal.review.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.message.DomainEvent;
import com.ezmeal.review.application.dto.command.ReviewDeleteCommand;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewDeleteMessage(
        UUID reviewId
) implements DomainEvent {
    // Message와 카프카 헤더에서 추출한 인증 정보를 조합해 비즈니스 Command로 변환
    public ReviewDeleteCommand toCommand(String userId, Role role) {
        return new ReviewDeleteCommand(
                this.reviewId,
                userId,
                role
        );
    }
}

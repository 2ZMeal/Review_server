package com.ezmeal.review.infrastructure.message.kafka.consumer.dto;

import com.ezmeal.review.application.dto.command.ReviewCreateCommand;

public record ReviewCreateMessage(
        String productId,
        int score,
        String contents
) {
    // Message를 Service에 넘기기 위해 Command로 변환하는 편의 메서드
    public ReviewCreateCommand toCommand(String userId) {
        return new ReviewCreateCommand(
                userId,
                this.productId,
                this.score,
                this.contents
        );
    }
}

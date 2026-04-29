package com.ezmeal.review.infrastructure.message.kafka.consumer.dto;

import java.time.LocalDateTime;

public record UserNicknameUpdatedMessage(
        String userId,
        String newNickname,
        LocalDateTime occurredAt
) {}

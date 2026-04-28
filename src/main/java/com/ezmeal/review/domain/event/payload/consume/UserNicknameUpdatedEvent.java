package com.ezmeal.review.domain.event.payload.consume;

import java.time.LocalDateTime;

public record UserNicknameUpdatedEvent(
        String userId,
        String newNickname,
        LocalDateTime occurredAt
) {}

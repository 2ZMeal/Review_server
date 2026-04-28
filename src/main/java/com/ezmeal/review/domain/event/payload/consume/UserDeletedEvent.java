package com.ezmeal.review.domain.event.payload.consume;

import java.time.LocalDateTime;

public record UserDeletedEvent(
        String userId,
        String deletedBy,
        LocalDateTime occurredAt
) {}

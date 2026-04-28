package com.ezmeal.review.domain.event.payload.consume;

import java.time.LocalDateTime;

public record ProductDeletedEvent(
        String productId,
        String deletedBy,
        LocalDateTime occurredAt
) {}

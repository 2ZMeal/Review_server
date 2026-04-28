package com.ezmeal.review.infrastructure.message.kafka.consumer.dto;

import java.time.LocalDateTime;

public record ProductDeletedMessage(
        String productId,
        String deletedBy,
        LocalDateTime occurredAt
) {}

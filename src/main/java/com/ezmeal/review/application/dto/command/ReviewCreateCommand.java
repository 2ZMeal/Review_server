package com.ezmeal.review.application.dto.command;

public record ReviewCreateCommand(
        String userId,
        String productId,
        int score,
        String contents
) {
}

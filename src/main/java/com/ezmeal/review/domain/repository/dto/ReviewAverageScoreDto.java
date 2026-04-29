package com.ezmeal.review.domain.repository.dto;

public record ReviewAverageScoreDto(
        String productId,
        long totalCount,
        double averageScore)
{}

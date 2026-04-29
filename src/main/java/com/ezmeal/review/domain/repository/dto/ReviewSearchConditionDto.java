package com.ezmeal.review.domain.repository.dto;

// 리뷰 검색 조건을 처리하기 위해 사용
public record ReviewSearchConditionDto(
        String productId,
        String userId,
        Integer minScore,
        Integer maxScore,
        String keyword
) {}

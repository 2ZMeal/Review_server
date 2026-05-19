package com.ezmeal.review.application.dto.response;

import com.ezmeal.review.domain.model.Review;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID reviewId,
        String userId,
        String nickname,
        String productId,
        int score,
        String contents,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    // Review 객체를 ReviewResponse DTO로 변환해주는 팩토리 메서드
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getReviewId(),
                review.getUserId(),
                review.getNickname(),
                review.getProductId(),
                review.getScore(),
                review.getContents(),
                review.getCreatedAt(),
                review.getModifiedAt()
        );
    }
}

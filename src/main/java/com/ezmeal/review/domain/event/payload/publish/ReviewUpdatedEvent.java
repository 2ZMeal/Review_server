package com.ezmeal.review.domain.event.payload.publish;

import com.ezmeal.review.domain.model.Review;
import java.time.LocalDateTime;

public record ReviewUpdatedEvent(
        String reviewId,
        String userId,
        String productId,
        int score,
        String contents,
        LocalDateTime occurredAt
) {
    public static ReviewUpdatedEvent from(Review review) {
        return new ReviewUpdatedEvent(
                review.getReviewId().toString(),
                review.getUserId(),
                review.getProductId(),
                review.getScore(),
                review.getContents(),
                review.getModifiedAt() != null ? review.getModifiedAt() : LocalDateTime.now()
        );
    }
}

package com.ezmeal.review.domain.event.payload.publish;

import com.ezmeal.common.message.DomainEvent;
import com.ezmeal.review.domain.model.Review;
import java.time.LocalDateTime;

public record ReviewDeletedEvent(
        String reviewId,
        String userId,
        String productId,
        int score,
        String deletedBy
) implements DomainEvent {
    public static ReviewDeletedEvent from(Review review) {
        return new ReviewDeletedEvent(
                review.getReviewId().toString(),
                review.getUserId(),
                review.getProductId(),
                review.getScore(),
                review.getDeletedBy()
        );
    }
}

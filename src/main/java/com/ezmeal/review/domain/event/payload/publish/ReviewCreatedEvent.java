package com.ezmeal.review.domain.event.payload.publish;

import com.ezmeal.common.message.DomainEvent;
import com.ezmeal.review.domain.model.Review;
import java.time.LocalDateTime;

public record ReviewCreatedEvent(
        String reviewId,
        String userId,
        String productId,
        int score,
        String contents
) implements DomainEvent {
    // Review 객체를 이벤트 페이로드로 바꿔주는 팩토리 메서드
    public static ReviewCreatedEvent from(Review review) {
        return new ReviewCreatedEvent(
                review.getReviewId().toString(),
                review.getUserId(),
                review.getProductId(),
                review.getScore(),
                review.getContents()
        );
    }
}

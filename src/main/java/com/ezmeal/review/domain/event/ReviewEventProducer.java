package com.ezmeal.review.domain.event;

import com.ezmeal.review.domain.event.payload.publish.ReviewCreatedEvent;
import com.ezmeal.review.domain.event.payload.publish.ReviewDeletedEvent;
import com.ezmeal.review.domain.event.payload.publish.ReviewUpdatedEvent;

public interface ReviewEventProducer {
    // 리뷰 생성 시
    void publishCreatedEvent(ReviewCreatedEvent event);
    // 리뷰 내용 수정 시
    void publishUpdatedEvent(ReviewUpdatedEvent event);
    // 리뷰 삭제 시
    void publishDeletedEvent(ReviewDeletedEvent event);
}

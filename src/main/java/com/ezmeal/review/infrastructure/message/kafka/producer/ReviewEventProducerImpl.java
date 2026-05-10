package com.ezmeal.review.infrastructure.message.kafka.producer;

import com.ezmeal.common.message.CommonKafkaEventPublisher;
import com.ezmeal.review.domain.event.ReviewEventProducer;
import com.ezmeal.review.domain.event.payload.publish.ReviewCreatedEvent;
import com.ezmeal.review.domain.event.payload.publish.ReviewDeletedEvent;
import com.ezmeal.review.domain.event.payload.publish.ReviewUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventProducerImpl implements ReviewEventProducer {

    // KafkaTemplate 대신 공통 모듈의 EventPublisher를 주입받습니다.
    private final CommonKafkaEventPublisher eventPublisher;

    @Value("${kafka.topic.review.created:review-created-topic}")
    private String reviewCreatedTopic;

    @Value("${kafka.topic.review.updated:review-updated-topic}")
    private String reviewUpdatedTopic;

    @Value("${kafka.topic.review.deleted:review-deleted-topic}")
    private String reviewDeletedTopic;

    @Override
    public void publishCreatedEvent(ReviewCreatedEvent event) {
        eventPublisher.publish(
                reviewCreatedTopic,
                event.reviewId(),      // aggregateId (순서 보장용 기준 키)
                "REVIEW_CREATED",      // eventType
                event                  // payload (DomainEvent 구현체)
        );
    }

    @Override
    public void publishUpdatedEvent(ReviewUpdatedEvent event) {
        eventPublisher.publish(
                reviewUpdatedTopic,
                event.reviewId(),
                "REVIEW_UPDATED",
                event
        );
    }

    @Override
    public void publishDeletedEvent(ReviewDeletedEvent event) {
        eventPublisher.publish(
                reviewDeletedTopic,
                event.reviewId(),
                "REVIEW_DELETED",
                event
        );
    }

}

package com.ezmeal.review.infrastructure.message.kafka.producer;

import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.review.domain.event.ReviewEventProducer;
import com.ezmeal.review.domain.event.payload.publish.ReviewCreatedEvent;
import com.ezmeal.review.domain.event.payload.publish.ReviewDeletedEvent;
import com.ezmeal.review.domain.event.payload.publish.ReviewUpdatedEvent;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventProducerImpl implements ReviewEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.review.created:review-created-topic}")
    private String reviewCreatedTopic;

    @Value("${kafka.topic.review.updated:review-updated-topic}")
    private String reviewUpdatedTopic;

    @Value("${kafka.topic.review.deleted:review-deleted-topic}")
    private String reviewDeletedTopic;

    @Override
    public void publishCreatedEvent(ReviewCreatedEvent event) {
        // 헤더와 페이로드를 함께 담아서 보내는 sendWithHeaders를 사용
        sendWithHeaders(reviewCreatedTopic, event.reviewId(), event);
    }

    @Override
    public void publishUpdatedEvent(ReviewUpdatedEvent event) {
        // 헤더와 페이로드를 함께 담아서 보내는 sendWithHeaders를 사용
        sendWithHeaders(reviewUpdatedTopic, event.reviewId(), event);
    }

    @Override
    public void publishDeletedEvent(ReviewDeletedEvent event) {
        // 헤더와 페이로드를 함께 담아서 보내는 sendWithHeaders를 사용
        sendWithHeaders(reviewDeletedTopic, event.reviewId(), event);
    }

    // 카프카 이벤트 헤더에 사용자 정보 담기
    private void sendWithHeaders(String topic, String key, Object payload) {
        // 토픽, 키, 페이로드를 담은 record를 생성
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, payload);

        // 현재 스레드의 인증 정보(SecurityContext)를 가져옴
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 있는 유저의 요청일 경우에만 카프카 헤더 추가
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            record.headers().add("X-User-Id", principal.getUserId().getBytes(StandardCharsets.UTF_8));
            record.headers().add("X-User-Roles", principal.getRole().name().getBytes(StandardCharsets.UTF_8));
        }

        // 카프카 전송
        kafkaTemplate.send(record);
    }
}

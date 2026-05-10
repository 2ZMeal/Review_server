package com.ezmeal.review.infrastructure.message.kafka.consumer;

import com.ezmeal.common.enums.Role;
import com.ezmeal.common.message.EventEnvelope;
import com.ezmeal.common.message.inbox.InboxProcessor;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.review.application.dto.command.ReviewCreateCommand;
import com.ezmeal.review.application.dto.command.ReviewDeleteCommand;
import com.ezmeal.review.application.dto.command.ReviewUpdateCommand;
import com.ezmeal.review.application.service.ReviewService;
import com.ezmeal.review.infrastructure.message.kafka.consumer.dto.ProductDeletedMessage;
import com.ezmeal.review.infrastructure.message.kafka.consumer.dto.ReviewCreateMessage;
import com.ezmeal.review.infrastructure.message.kafka.consumer.dto.ReviewDeleteMessage;
import com.ezmeal.review.infrastructure.message.kafka.consumer.dto.ReviewUpdateMessage;
import com.ezmeal.review.infrastructure.message.kafka.consumer.dto.UserDeletedMessage;
import com.ezmeal.review.infrastructure.message.kafka.consumer.dto.UserNickNameUpdatedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventListenerImpl {

    private final ReviewService reviewService;
    private final InboxProcessor inboxProcessor;

    // ==================
    // 일반 C, U, D 이벤트
    // ==================

    @KafkaListener(topics = "review-create-command-topic", groupId = "${spring.kafka.consumer.group-id:review-group}")
    public void handleReviewCreate(EventEnvelope<ReviewCreateMessage> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();

            if (!"SYSTEM".equals(principal.getUserId())) {
                log.info("[Kafka] 유저({})의 요청으로 리뷰를 생성합니다. 대상 상품: {}", principal.getUserId(), envelope.payload().productId());
                ReviewCreateCommand command = envelope.payload().toCommand(principal.getUserId());
                reviewService.createReview(command);
            } else {
                log.warn("[Kafka] 리뷰 생성은 유저 권한이 필수입니다. 시스템 생성을 지원하지 않습니다.");
            }
        });
    }

    @KafkaListener(topics = "review-update-command-topic", groupId = "${spring.kafka.consumer.group-id:review-group}")
    public void handleReviewUpdate(EventEnvelope<ReviewUpdateMessage> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();

            if ("SYSTEM".equals(principal.getUserId())) {
                log.warn("[Kafka] 시스템에 의한 단건 리뷰 수정은 지원하지 않습니다.");
            } else {
                log.info("[Kafka] 유저({})의 요청으로 리뷰({})를 수정합니다.", principal.getUserId(), envelope.payload().reviewId());
                ReviewUpdateCommand command = envelope.payload().toCommand(principal.getUserId(), principal.getRole());
                reviewService.updateReview(command);
            }
        });
    }

    @KafkaListener(topics = "review-delete-command-topic", groupId = "${spring.kafka.consumer.group-id:review-group}")
    public void handleReviewDelete(EventEnvelope<ReviewDeleteMessage> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();

            if ("SYSTEM".equals(principal.getUserId())) {
                log.info("[Kafka] 시스템 요청으로 리뷰({})를 삭제합니다.", envelope.payload().reviewId());
                ReviewDeleteCommand systemCommand = envelope.payload().toCommand("SYSTEM", Role.ADMIN);
                reviewService.deleteReview(systemCommand);
            } else {
                log.info("[Kafka] 유저({})의 요청으로 리뷰({})를 삭제합니다.", principal.getUserId(), envelope.payload().reviewId());
                ReviewDeleteCommand command = envelope.payload().toCommand(principal.getUserId(), principal.getRole());
                reviewService.deleteReview(command);
            }
        });
    }

    // ==========================================
    // 일괄 처리 이벤트 (타 서버에서 발생한 이벤트 수신)
    // ==========================================

    @KafkaListener(topics = "${kafka.topic.user.nickname.updated:user.updated}", groupId = "${spring.kafka.consumer.group-id:customer-group}")
    public void consumeUserNicknameUpdatedEvent(EventEnvelope<UserNickNameUpdatedMessage> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();
            log.info("[Kafka] ({})의 요청으로 닉네임 일괄 변경을 수행합니다. 대상 유저: {}", principal.getUserId(), envelope.payload().userId());
            reviewService.bulkUpdateNicknameByUserId(envelope.payload().userId(), envelope.payload().nickname());
        });
    }

    @KafkaListener(topics = "${kafka.topic.user.deleted:user.deleted}", groupId = "${spring.kafka.consumer.group-id:customer-group}")
    public void consumeUserDeletedEvent(EventEnvelope<UserDeletedMessage> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();
            log.info("[Kafka] ({})의 요청으로 유저의 모든 리뷰 일괄 삭제를 수행합니다. 대상 유저: {}", principal.getUserId(), envelope.payload().userId());
            reviewService.bulkSoftDeleteByUserId(envelope.payload().userId(), principal.getUserId());
        });
    }

    @KafkaListener(topics = "${kafka.topic.product.deleted:product-deleted-topic}", groupId = "${spring.kafka.consumer.group-id:review-group}")
    public void consumeProductDeletedEvent(EventEnvelope<ProductDeletedMessage> envelope) {
        inboxProcessor.processOnce(envelope.eventId(), () -> {
            CustomUserPrincipal principal = getCurrentPrincipal();
            log.info("[Kafka] ({})의 요청으로 상품의 모든 리뷰 일괄 삭제를 수행합니다. 대상 상품: {}", principal.getUserId(), envelope.payload().productId());
            reviewService.bulkSoftDeleteByProductId(envelope.payload().productId(), principal.getUserId());
        });
    }

    // ===================
    // 내부 공통 유틸 메서드 (리팩토링됨)
    // ===================

    /**
     * 공통 모듈의 KafkaSecurityInterceptor가 SecurityContext에 넣어둔 유저 정보를 꺼내옵니다.
     * 정보가 없다면 시스템(SYSTEM)의 동작으로 간주합니다.
     */
    private CustomUserPrincipal getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return principal;
        }

        // 헤더에 인증 정보가 없는 경우 SYSTEM 계정으로 간주
        return new CustomUserPrincipal("SYSTEM", Role.ADMIN, "system@ezmeal.com");
    }
}

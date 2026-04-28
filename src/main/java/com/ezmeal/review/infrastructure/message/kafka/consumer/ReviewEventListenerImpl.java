package com.ezmeal.review.infrastructure.message.kafka.consumer;

import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.review.application.service.ReviewService;
import com.ezmeal.review.domain.event.ReviewEventConsumer;
import com.ezmeal.review.domain.event.payload.consume.ProductDeletedEvent;
import com.ezmeal.review.domain.event.payload.consume.UserDeletedEvent;
import com.ezmeal.review.domain.event.payload.consume.UserNicknameUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventListenerImpl implements ReviewEventConsumer {

    private final ReviewService reviewService;

    // 닉네임 변경
    @Override
    @KafkaListener(topics = "${kafka.topic.user.nickname.updated:user-nickname-updated-topic}", groupId = "${spring.kafka.consumer.group-id:review-group}")
    public void consumeUserNicknameUpdatedEvent(UserNicknameUpdatedEvent event) {
        CustomUserPrincipal principal = getAuthenticatedUser();

        try {
            if (principal != null) {
                log.info("[Kafka] 유저({})의 요청으로 닉네임 일괄 변경을 수행합니다. 대상 유저: {}", principal.getUserId(), event.userId());
            } else {
                log.info("[Kafka] 시스템 요청으로 닉네임 일괄 변경을 수행합니다. 대상 유저: {}", event.userId());
            }
            reviewService.updateNicknameBulk(event.userId(), event.newNickname());
        } catch (Exception e) {
            log.error("[Kafka] 회원 닉네임 변경 이벤트 처리 중 예외 발생", e);
        }
    }

    // 회원 탈퇴
    @Override
    @KafkaListener(topics = "${kafka.topic.user.deleted:user-deleted-topic}", groupId = "${spring.kafka.consumer.group-id:review-group}")
    public void consumeUserDeletedEvent(UserDeletedEvent event) {
        CustomUserPrincipal principal = getAuthenticatedUser();

        String deletedBy = (principal != null) ? principal.getUserId() : "SYSTEM";

        try {
            if (principal != null) {
                log.info("[Kafka] 유저({})의 요청으로 유저의 모든 리뷰 일괄 삭제를 수행합니다. 대상 유저: {}", principal.getUserId(), event.userId());
            } else {
                log.info("[Kafka] 시스템 요청으로 유저의 모든 리뷰 일괄 삭제를 수행합니다. 대상 유저: {}", event.userId());
            }
            reviewService.deleteBulkByUser(event.userId(), deletedBy);
        } catch (Exception e) {
            log.error("[Kafka] 회원 탈퇴 이벤트 처리 중 예외 발생", e);
        }
    }

    // 상품 삭제 시
    @Override
    @KafkaListener(topics = "${kafka.topic.product.deleted:product-deleted-topic}", groupId = "${spring.kafka.consumer.group-id:review-group}")
    public void consumeProductDeletedEvent(ProductDeletedEvent event) {
        CustomUserPrincipal principal = getAuthenticatedUser();
        String deletedBy = (principal != null) ? principal.getUserId() : "SYSTEM";

        try {
            if (principal != null) {
                log.info("[Kafka] 유저({})의 요청으로 상품의 모든 리뷰 일괄 삭제를 수행합니다. 대상 상품: {}", principal.getUserId(), event.productId());
            } else {
                log.info("[Kafka] 시스템 요청으로 상품의 모든 리뷰 일괄 삭제를 수행합니다. 대상 상품: {}", event.productId());
            }
            reviewService.deleteBulkByProduct(event.productId(), deletedBy);
        } catch (Exception e) {
            log.error("[Kafka] 상품 삭제 이벤트 처리 중 예외 발생", e);
        }
    }

    // 내부 공통 유틸 (인증된 유저인지 확인)
    private CustomUserPrincipal getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 존재하고, CustomUserPrincipal일 경우 반환
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return principal;
        }

        // 헤더가 없어서 인터셉터가 채우지 못한 경우 (순수 시스템 호출)
        return null;
    }
}

package com.ezmeal.review.infrastructure.message.kafka.consumer;

import com.ezmeal.common.enums.Role;
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
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventListenerImpl {

    private final ReviewService reviewService;

    // ==================
    // 일반 C, U, D 이벤트
    // ==================

    // 리뷰 생성 메시지 수신
    @KafkaListener(topics = "review-create-command-topic", groupId = "${spring.kafka.consumer.group-id:review-group}", containerFactory = "customKafkaListenerContainerFactory")
    public void handleReviewCreate(
            @Payload ReviewCreateMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes
    ) {
        String userId = extractStringHeader(userIdBytes, "SYSTEM");

        if (!"SYSTEM".equals(userId)) {
            log.info("[Kafka] 유저({})의 요청으로 리뷰를 생성합니다. 대상 상품: {}", userId, message.productId());
            ReviewCreateCommand command = message.toCommand(userId);
            reviewService.createReview(command);
        } else {
            log.warn("[Kafka] 리뷰 생성은 유저 권한이 필수입니다. 시스템 생성을 지원하지 않습니다.");
        }
    }

    // 리뷰 수정 메시지 수신
    @KafkaListener(topics = "review-update-command-topic", groupId = "${spring.kafka.consumer.group-id:review-group}", containerFactory = "customKafkaListenerContainerFactory")
    public void handleReviewUpdate(
            @Payload ReviewUpdateMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes,
            @Header(value = "X-User-Roles", required = false) byte[] roleBytes
    ) {
        String userId = extractStringHeader(userIdBytes, "SYSTEM");

        if ("SYSTEM".equals(userId)) {
            log.warn("[Kafka] 시스템에 의한 단건 리뷰 수정은 지원하지 않습니다.");
        } else {
            // 일반 유저인 경우에만 엄격한 Role 검사 수행
            Role role = extractRoleHeader(roleBytes);

            log.info("[Kafka] 유저({})의 요청으로 리뷰({})를 수정합니다.", userId, message.reviewId());
            ReviewUpdateCommand command = message.toCommand(userId, role);
            reviewService.updateReview(command);
        }
    }

    // 리뷰 삭제 메시지 수신
    @KafkaListener(topics = "review-delete-command-topic", groupId = "${spring.kafka.consumer.group-id:review-group}", containerFactory = "customKafkaListenerContainerFactory")
    public void handleReviewDelete(
            @Payload ReviewDeleteMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes,
            @Header(value = "X-User-Roles", required = false) byte[] roleBytes
    ) {
        String userId = extractStringHeader(userIdBytes, "SYSTEM");

        if ("SYSTEM".equals(userId)) {
            log.info("[Kafka] 시스템 요청으로 리뷰({})를 삭제합니다.", message.reviewId());
            ReviewDeleteCommand systemCommand = message.toCommand("SYSTEM", Role.ADMIN);
            reviewService.deleteReview(systemCommand);
        } else {
            // 일반 유저인 경우에만 엄격한 Role 검사 수행
            Role role = extractRoleHeader(roleBytes);

            log.info("[Kafka] 유저({})의 요청으로 리뷰({})를 삭제합니다.", userId, message.reviewId());
            ReviewDeleteCommand command = message.toCommand(userId, role);
            reviewService.deleteReview(command);
        }
    }

    // ==========================================
    // 일괄 처리 이벤트 (타 서버에서 발생한 이벤트 수신)
    // ==========================================

    @KafkaListener(topics = "${kafka.topic.user.nickname.updated:user.updated}", groupId = "${spring.kafka.consumer.group-id:customer-group}", containerFactory = "customKafkaListenerContainerFactory")
    public void consumeUserNicknameUpdatedEvent(
            @Payload UserNickNameUpdatedMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes
    ) {
        String userId = extractStringHeader(userIdBytes, "SYSTEM");
        log.info("[Kafka] ({})의 요청으로 닉네임 일괄 변경을 수행합니다. 대상 유저: {}", userId, message.userId());
        reviewService.bulkUpdateNicknameByUserId(message.userId(), message.userNickname());
    }

    @KafkaListener(topics = "${kafka.topic.user.deleted:user.deleted}", groupId = "${spring.kafka.consumer.group-id:customer-group}", containerFactory = "customKafkaListenerContainerFactory")
    public void consumeUserDeletedEvent(
            @Payload UserDeletedMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes
    ) {
        String deletedBy = extractStringHeader(userIdBytes, "SYSTEM");
        log.info("[Kafka] ({})의 요청으로 유저의 모든 리뷰 일괄 삭제를 수행합니다. 대상 유저: {}", deletedBy, message.userId());
        reviewService.bulkSoftDeleteByUserId(message.userId(), deletedBy);
    }

    @KafkaListener(topics = "${kafka.topic.product.deleted:product-deleted-topic}", groupId = "${spring.kafka.consumer.group-id:review-group}", containerFactory = "customKafkaListenerContainerFactory")
    public void consumeProductDeletedEvent(
            @Payload ProductDeletedMessage message,
            @Header(value = "X-User-Id", required = false) byte[] userIdBytes
    ) {
        String deletedBy = extractStringHeader(userIdBytes, "SYSTEM");
        log.info("[Kafka] ({})의 요청으로 상품의 모든 리뷰 일괄 삭제를 수행합니다. 대상 상품: {}", deletedBy, message.productId());
        reviewService.bulkSoftDeleteByProductId(message.productId(), deletedBy);
    }

    // ===================
    // 내부 공통 유틸 메서드
    // ===================

    // 헤더에서 넘어온 바이트를 문자열로 변환
    private String extractStringHeader(byte[] headerBytes, String defaultValue) {
        if (headerBytes == null || headerBytes.length == 0) {
            return defaultValue;
        }
        return new String(headerBytes, StandardCharsets.UTF_8);
    }

    // 헤더 역할 검증
    private Role extractRoleHeader(byte[] roleBytes) {
        // 헤더가 누락된 경우 (유저 요청인데 Role이 없으면 비정상 접근으로 간주)
        if (roleBytes == null || roleBytes.length == 0) {
            log.error("[Kafka] 필수 헤더인 X-User-Roles이 누락되었습니다.");
            throw new IllegalArgumentException("필수 권한 헤더(X-User-Roles)가 없습니다.");
        }

        // 헤더 값을 Enum으로 변환
        try {
            String roleStr = new String(roleBytes, StandardCharsets.UTF_8);
            return Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            log.error("[Kafka] 알 수 없는 Role 헤더 값입니다: {}", new String(roleBytes, StandardCharsets.UTF_8));
            throw new IllegalArgumentException("유효하지 않은 권한 헤더 값입니다.");
        }
    }
}

package com.ezmeal.review.domain.event;

import com.ezmeal.review.domain.event.payload.consume.ProductDeletedEvent;
import com.ezmeal.review.domain.event.payload.consume.UserDeletedEvent;
import com.ezmeal.review.domain.event.payload.consume.UserNicknameUpdatedEvent;

public interface ReviewEventConsumer {
    // 회원 닉네임 변경 시
    void consumeUserNicknameUpdatedEvent(UserNicknameUpdatedEvent event);

    // 회원 탈퇴 시
    void consumeUserDeletedEvent(UserDeletedEvent event);

    // 상품 삭제 시
    void consumeProductDeletedEvent(ProductDeletedEvent event);
}

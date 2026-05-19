package com.ezmeal.review.presentation.request;

import com.ezmeal.review.application.dto.command.ReviewCreateCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotBlank(message = "상품 ID는 필수 입력값입니다.")
        String productId,

        @NotNull(message = "평점은 필수 입력값입니다.")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        Integer score,

        String contents
) {
    // Request를 Service에 넘기기 위해 Command로 변환하는 편의 메서드
    public ReviewCreateCommand toCommand(String userId) {
        return new ReviewCreateCommand(
                userId,
                this.productId,
                this.score,
                this.contents
        );
    }
}

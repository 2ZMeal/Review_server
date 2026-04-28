package com.ezmeal.review.presentation.request;

import com.ezmeal.common.enums.Role;
import com.ezmeal.review.application.dto.command.ReviewUpdateCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

// 웹 입력값 검증 전용 DTO
public record ReviewUpdateRequest(
        @NotBlank(message = "닉네임은 필수 입력값입니다.")
        String nickname,

        @NotNull(message = "평점은 필수 입력값입니다.")
        @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
        Integer score,

        String contents
) {
    // Request와 경로변수(reviewId), 인증정보(userId, role)를 조합해 Command로 변환
    public ReviewUpdateCommand toCommand(UUID reviewId, String userId, Role role) {
        return new ReviewUpdateCommand(
                reviewId,
                userId,
                role,
                this.nickname,
                this.score,
                this.contents
        );
    }
}

package com.ezmeal.review.domain.exception;

import com.ezmeal.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_404_1", "해당 리뷰를 찾을 수 없습니다."),
    REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN, "REVIEW_403_1", "리뷰를 수정하거나 삭제할 권한이 없습니다."),
    INVALID_SCORE(HttpStatus.BAD_REQUEST, "REVIEW_400_1", "리뷰 평점은 1점에서 5점 사이여야 합니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "REVIEW_400_2", "필수 입력값 검증에 실패하였습니다."),
    ALREADY_REVIEWED(HttpStatus.CONFLICT, "REVIEW_409_1", "이미 해당 상품에 대한 리뷰를 작성하셨습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

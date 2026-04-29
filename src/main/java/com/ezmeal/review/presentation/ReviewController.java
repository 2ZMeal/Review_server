package com.ezmeal.review.presentation;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.review.application.dto.command.ReviewCreateCommand;
import com.ezmeal.review.application.dto.command.ReviewUpdateCommand;
import com.ezmeal.review.application.dto.response.ReviewResponse;
import com.ezmeal.review.application.service.ReviewService;
import com.ezmeal.review.domain.repository.dto.ReviewAverageScoreDto;
import com.ezmeal.review.domain.repository.dto.ReviewSearchConditionDto;
import com.ezmeal.review.presentation.request.ReviewCreateRequest;
import com.ezmeal.review.presentation.request.ReviewUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 생성
    @PostMapping
    public ResponseEntity<CommonApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewCreateCommand command = request.toCommand(principal.getUserId());
        ReviewResponse response = reviewService.createReview(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonApiResponse.success(response));
    }

    // 리뷰 수정
    @PatchMapping("/{reviewId}")
    public ResponseEntity<CommonApiResponse<ReviewResponse>> updateReview(
            @PathVariable("reviewId") UUID reviewId,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        ReviewUpdateCommand command = request.toCommand(reviewId, principal.getUserId(), principal.getRole());
        ReviewResponse response = reviewService.updateReview(command);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    // 리뷰 단건 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<CommonApiResponse<ReviewResponse>> getReview(
            @PathVariable("reviewId") UUID reviewId
    ) {
        ReviewResponse response = reviewService.getReview(reviewId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    // 리뷰 목록 조회 (필터링 및 페이징 포함)
    @GetMapping
    public ResponseEntity<CommonApiResponse<Page<ReviewResponse>>> searchReviews(
            @ModelAttribute ReviewSearchConditionDto condition,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<ReviewResponse> response = reviewService.searchReviews(condition, pageable);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

    // 특정 상품의 리뷰 평점 통계 조회
    @GetMapping("/statistics/products/{productId}")
    public ResponseEntity<CommonApiResponse<ReviewAverageScoreDto>> getReviewStatistics(
            @PathVariable("productId") String productId
    ) {
        ReviewAverageScoreDto response = reviewService.getReviewStatistics(productId);

        return ResponseEntity.ok(CommonApiResponse.success(response));
    }

}

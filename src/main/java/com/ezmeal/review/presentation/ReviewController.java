package com.ezmeal.review.presentation;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.review.application.dto.command.ReviewCreateCommand;
import com.ezmeal.review.application.dto.command.ReviewUpdateCommand;
import com.ezmeal.review.application.dto.response.ReviewResponse;
import com.ezmeal.review.application.service.ReviewService;
import com.ezmeal.review.presentation.request.ReviewCreateRequest;
import com.ezmeal.review.presentation.request.ReviewUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping
    public ResponseEntity<CommonApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewCreateCommand command = request.toCommand(principal.getUserId());
        ReviewResponse response = reviewService.createReview(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonApiResponse.success(response));
    }

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

}

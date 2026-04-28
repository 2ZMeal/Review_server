package com.ezmeal.review.presentation;

import com.ezmeal.common.response.CommonApiResponse;
import com.ezmeal.common.security.principal.CustomUserPrincipal;
import com.ezmeal.review.application.dto.command.ReviewCreateCommand;
import com.ezmeal.review.application.dto.response.ReviewResponse;
import com.ezmeal.review.application.service.ReviewService;
import com.ezmeal.review.presentation.request.ReviewCreateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
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

}

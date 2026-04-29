package com.ezmeal.review.application.service;

import com.ezmeal.common.exception.types.ConflictException;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.common.exception.types.NotFoundException;
import com.ezmeal.review.application.dto.command.ReviewCreateCommand;
import com.ezmeal.review.application.dto.command.ReviewUpdateCommand;
import com.ezmeal.review.application.dto.response.ReviewResponse;
import com.ezmeal.review.domain.event.ReviewEventProducer;
import com.ezmeal.review.domain.event.payload.publish.ReviewCreatedEvent;
import com.ezmeal.review.domain.event.payload.publish.ReviewUpdatedEvent;
import com.ezmeal.review.domain.exception.ReviewErrorCode;
import com.ezmeal.review.domain.model.Review;
import com.ezmeal.review.domain.provider.UserData;
import com.ezmeal.review.domain.provider.UserProvider;
import com.ezmeal.review.domain.repository.ReviewRepository;
import com.ezmeal.review.domain.repository.dto.ReviewAverageScoreDto;
import com.ezmeal.review.domain.repository.dto.ReviewSearchConditionDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewEventProducer eventProducer;
    private final UserProvider userProvider;

    /*
    Feign Client가 @Transaction 내부에서 가져오는 경우면
    DB 커넥션 풀이 고갈될 수 있음 (가져오는게 늦어지는 경우에)
    이를 위해 transaction 범위를 세밀하게 지정할 수 있는 transactionTemplate를 사용
    */
    private final TransactionTemplate transactionTemplate;

    // =============================================
    // API로 호출하는 경우와 이벤트를 수신했을 때를 모두 대응
    // =============================================

    // 리뷰 생성
    public ReviewResponse createReview(ReviewCreateCommand command) {
        UserData userData = userProvider.getUser(command.userId());

        Review savedReview;
        try {
            // 트랜잭션 내부
            savedReview = transactionTemplate.execute(status -> {
                // 삭제 여부와 상관없이 유저-상품으로 리뷰 조회
                Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(command.userId(), command.productId());

                if (existingReview.isPresent()) {
                    Review review = existingReview.get();
                    if (review.getDeletedAt() == null) {
                        throw new ConflictException(ReviewErrorCode.ALREADY_REVIEWED);
                    } else {
                        throw new ForbiddenException(ReviewErrorCode.CANNOT_REWRITE_DELETED_REVIEW);
                    }
                }

                // 리뷰가 아예 없었던 경우만 정상 생성
                Review review = Review.create(command.userId(), userData.nickname(), command.productId(), command.score(), command.contents());
                return reviewRepository.save(review);
            });
        } catch (DataIntegrityViolationException e) {
            // 동시성 문제로 DB Unique 에러가 발생하면 예외 발생 (빠르게 따닥 클릭하는 경우)
            log.warn("동시 리뷰 작성 요청 발생: userId={}, productId={}", command.userId(), command.productId());
            throw new ConflictException(ReviewErrorCode.ALREADY_REVIEWED);
        }

        // 이벤트 발행
        eventProducer.publishCreatedEvent(ReviewCreatedEvent.from(savedReview));

        return ReviewResponse.from(savedReview);
    }

    // 리뷰 수정
    public ReviewResponse updateReview(ReviewUpdateCommand command) {
        // 트랜잭션 내부
        Review updatedReview = transactionTemplate.execute(status -> {
            // 존재하는지 확인
            Review review = reviewRepository.findActiveById(command.reviewId())
                    .orElseThrow(() -> new NotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND));

            review.updateReview(command.userId(), command.role(), command.score(), command.contents());
            return review;
        });

        // 이벤트 발행
        eventProducer.publishUpdatedEvent(ReviewUpdatedEvent.from(updatedReview));

        return ReviewResponse.from(updatedReview);
    }

    // 단건 리뷰 상세 조회
    @Transactional(readOnly = true)
    public ReviewResponse getReview(java.util.UUID reviewId) {
        Review review = reviewRepository.findActiveById(reviewId)
                .orElseThrow(() -> new NotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND));
        return ReviewResponse.from(review);
    }

    // 다건 조건 검색 및 페이징 조회
    @Transactional(readOnly = true)
    public Page<ReviewResponse> searchReviews(ReviewSearchConditionDto condition, Pageable pageable) {
        // QueryDSL 레포지토리를 통해 엔티티 Page 조회
        Page<Review> reviewPage = reviewRepository.searchActiveReviews(condition, pageable);

        // 엔티티를 DTO로 변환하여 반환 (Page 인터페이스의 map 활용)
        return reviewPage.map(ReviewResponse::from);
    }

    // 특정 상품의 리뷰 평균 통계 조회
    @Transactional(readOnly = true)
    public ReviewAverageScoreDto getReviewStatistics(String productId) {
        return reviewRepository.getReviewAverageScoreByProductId(productId);
    }

}

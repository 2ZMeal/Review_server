package com.ezmeal.review.application.service;

import com.ezmeal.common.exception.types.ConflictException;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.common.exception.types.NotFoundException;
import com.ezmeal.review.application.dto.command.ReviewCreateCommand;
import com.ezmeal.review.application.dto.command.ReviewDeleteCommand;
import com.ezmeal.review.application.dto.command.ReviewUpdateCommand;
import com.ezmeal.review.application.dto.response.ReviewResponse;
import com.ezmeal.review.domain.event.ReviewEventProducer;
import com.ezmeal.review.domain.event.payload.publish.ReviewCreatedEvent;
import com.ezmeal.review.domain.event.payload.publish.ReviewDeletedEvent;
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
    private final TransactionTemplate transactionTemplate;

    // =============================================
    // API로 호출하는 경우와 이벤트를 수신했을 때를 모두 대응
    // =============================================

    // 리뷰 생성 (외부 API 통신이 있으므로 TransactionTemplate 사용)
    public ReviewResponse createReview(ReviewCreateCommand command) {
        // Feign Client 호출 (DB 커넥션을 물고 있지 않음)
        UserData userData = userProvider.getUser(command.userId());

        Review savedReview;
        try {
            // 비즈니스 로직 + Outbox 이벤트 발행을 하나의 트랜잭션으로 묶음
            savedReview = transactionTemplate.execute(status -> {

                Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(command.userId(), command.productId());

                if (existingReview.isPresent()) {
                    Review review = existingReview.get();
                    if (review.getDeletedAt() == null) {
                        throw new ConflictException(ReviewErrorCode.ALREADY_REVIEWED);
                    } else {
                        throw new ForbiddenException(ReviewErrorCode.CANNOT_REWRITE_DELETED_REVIEW);
                    }
                }

                Review review = Review.create(command.userId(), userData.nickname(), command.productId(), command.score(), command.contents());
                Review saved = reviewRepository.save(review);

                // 트랜잭션이 끝나기 전에 이벤트 발행 (Outbox DB에 INIT 상태로 함께 저장)
                eventProducer.publishCreatedEvent(ReviewCreatedEvent.from(saved));

                return saved;
            });
        } catch (DataIntegrityViolationException e) {
            log.warn("동시 리뷰 작성 요청 발생: userId={}, productId={}", command.userId(), command.productId());
            throw new ConflictException(ReviewErrorCode.ALREADY_REVIEWED);
        }

        return ReviewResponse.from(savedReview);
    }

    // 리뷰 수정 (외부 통신이 없으므로 @Transactional 사용)
    @Transactional
    public ReviewResponse updateReview(ReviewUpdateCommand command) {
        Review review = reviewRepository.findActiveById(command.reviewId())
                .orElseThrow(() -> new NotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND));

        review.updateReview(command.userId(), command.role(), command.score(), command.contents());

        // 트랜잭션 내부에서 이벤트 발행 (Outbox 연동)
        eventProducer.publishUpdatedEvent(ReviewUpdatedEvent.from(review));

        return ReviewResponse.from(review);
    }

    // 리뷰 삭제 (외부 통신이 없으므로 @Transactional 사용)
    @Transactional
    public void deleteReview(ReviewDeleteCommand command) {
        Review review = reviewRepository.findActiveById(command.reviewId())
                .orElseThrow(() -> new NotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (review.getDeletedAt() == null) {
            review.delete(command.userId(), command.role());

            // 트랜잭션 내부에서 이벤트 발행 (Outbox 연동)
            eventProducer.publishDeletedEvent(ReviewDeletedEvent.from(review));
        } else {
            log.info("이미 삭제 처리된 리뷰입니다. reviewId: {}", command.reviewId());
        }
    }

    // =============================================
    // 단순 조회 메서드들 (@Transactional(readOnly = true))
    // =============================================

    @Transactional(readOnly = true)
    public ReviewResponse getReview(java.util.UUID reviewId) {
        Review review = reviewRepository.findActiveById(reviewId)
                .orElseThrow(() -> new NotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND));
        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> searchReviews(ReviewSearchConditionDto condition, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.searchActiveReviews(condition, pageable);
        return reviewPage.map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public ReviewAverageScoreDto getReviewStatistics(String productId) {
        return reviewRepository.getReviewAverageScoreByProductId(productId);
    }

    // =============================================
    // 일괄 처리 로직 (외부 통신 없으므로 @Transactional로 리팩토링)
    // =============================================

    @Transactional
    public void bulkUpdateNicknameByUserId(String userId, String newNickname) {
        reviewRepository.bulkUpdateNicknameByUserId(userId, newNickname);
        log.info("유저({})의 모든 리뷰 닉네임이 [{}]로 일괄 변경되었습니다.", userId, newNickname);
    }

    @Transactional
    public void bulkSoftDeleteByUserId(String userId, String deletedBy) {
        reviewRepository.bulkSoftDeleteByUserId(userId, deletedBy);
        log.info("유저({}) 탈퇴로 인해 작성한 모든 리뷰가 삭제 처리되었습니다. (deletedBy={})", userId, deletedBy);
    }

    @Transactional
    public void bulkSoftDeleteByProductId(String productId, String deletedBy) {
        reviewRepository.bulkSoftDeleteByProductId(productId, deletedBy);
        log.info("상품({}) 삭제로 인해 종속된 모든 리뷰가 삭제 처리되었습니다. (deletedBy={})", productId, deletedBy);
    }
}

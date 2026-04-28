package com.ezmeal.review.application.service;

import com.ezmeal.common.exception.types.ConflictException;
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
import feign.Feign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

        // Feign client를 통해 User 서버 호출 (트랜잭션 외부)
        UserData userData = userProvider.getUser(command.userId());

        // 트랜잭션 내부
        return transactionTemplate.execute(status -> {

            if (reviewRepository.existsActiveByUserIdAndProductId(command.userId(), command.productId())) {
                throw new ConflictException(ReviewErrorCode.ALREADY_REVIEWED);
            }

            Review review = Review.create(command.userId(), userData.nickname(), command.productId(), command.score(), command.contents());
            Review savedReview = reviewRepository.save(review);

            // 이벤트 발행
            eventProducer.publishCreatedEvent(ReviewCreatedEvent.from(savedReview));

            return ReviewResponse.from(savedReview);
        });
    }

    public ReviewResponse updateReview(ReviewUpdateCommand command) {
        // 트랜잭션 내부
        return transactionTemplate.execute(status -> {
            // 존재하는지 확인
            Review review = reviewRepository.findActiveById(command.reviewId())
                    .orElseThrow(() -> new NotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND));

            review.updateReview(command.userId(), command.role(), command.nickname(), command.score(), command.contents());

            // 이벤트 발행
            eventProducer.publishUpdatedEvent(ReviewUpdatedEvent.from(review));

            return ReviewResponse.from(review);
        });
    }

    public void deleteReview(ReviewDeleteCommand command) {
        // 트랜잭션 내부
        transactionTemplate.executeWithoutResult(status -> {
            // 존재하는지 확인
            Review review = reviewRepository.findActiveById(command.reviewId())
                    .orElseThrow(() -> new NotFoundException(ReviewErrorCode.REVIEW_NOT_FOUND));

            review.delete(command.userId(), command.role());

            // 이벤트 발행
            eventProducer.publishDeletedEvent(ReviewDeletedEvent.from(review));
        });
    }

}

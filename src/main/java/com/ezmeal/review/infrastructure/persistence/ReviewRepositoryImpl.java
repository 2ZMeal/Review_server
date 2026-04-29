package com.ezmeal.review.infrastructure.persistence;

import com.ezmeal.review.domain.model.Review;
import com.ezmeal.review.domain.repository.ReviewRepository;
import com.ezmeal.review.domain.repository.dto.ReviewAverageScoreDto;
import com.ezmeal.review.domain.repository.dto.ReviewSearchConditionDto;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final JpaReviewRepository jpaReviewRepository;
    private final QueryDslReviewRepository queryDslReviewRepository;

    /*
    * ===================
    * JPA Repository 구역
    * ===================
    * */

    // 저장
    @Override
    public Review save(Review review) {
        return jpaReviewRepository.save(review);
    }

    // 단건 조회
    @Override
    public Optional<Review> findActiveById(UUID reviewId) {
        return jpaReviewRepository.findActiveById(reviewId);
    }

    // 멱등성 검사 1 (이미 존재하는 리뷰인지 검사)
    @Override
    public boolean existsActiveByUserIdAndProductId(String userId, String productId) {
        return jpaReviewRepository.existsByUserIdAndProductIdAndDeletedAtIsNull(userId, productId);
    }

    // 멱등성 검사 2 (삭제했던 리뷰도 포함) // createReview
    @Override
    public Optional<Review> findByUserIdAndProductId(String userId, String productId) {
        return jpaReviewRepository.findByUserIdAndProductId(userId, productId);
    }

    // 멱등성 검사 3 (삭제 여부 상관 없이 조회) // deleteReview (reviewId만 존재)
    @Override
    public Optional<Review> findById(UUID reviewId) {
        return jpaReviewRepository.findById(reviewId);
    }


    // 사용자 닉네임 변경 시 일괄 변경
    @Override
    public void bulkUpdateNicknameByUserId(String userId, String newNickname) {
        jpaReviewRepository.bulkUpdateNicknameByUserId(userId, newNickname);
    }

    // 사용자 탈퇴 시 일괄 삭제
    @Override
    public void bulkSoftDeleteByUserId(String userId, String deletedBy) {
        jpaReviewRepository.bulkSoftDeleteByUserId(userId, deletedBy);
    }

    // 상품 삭제 시 일괄 삭제
    @Override
    public void bulkSoftDeleteByProductId(String productId, String deletedBy) {
        jpaReviewRepository.bulkSoftDeleteByProductId(productId, deletedBy);
    }

    /*
     * ==============
     * Query DSL 구역
     * ==============
     * */

    // 필터에 따른 조건별 검색
    @Override
    public Page<Review> searchActiveReviews(ReviewSearchConditionDto condition, Pageable pageable) {
        return queryDslReviewRepository.searchActiveReviews(condition, pageable);
    }

    // 상품의 평균 리뷰 점수 계산
    @Override
    public ReviewAverageScoreDto getReviewAverageScoreByProductId(String productId) {
        return queryDslReviewRepository.getReviewAverageScoreByProductId(productId);
    }

}

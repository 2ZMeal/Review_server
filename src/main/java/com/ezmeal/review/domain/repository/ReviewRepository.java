package com.ezmeal.review.domain.repository;

import com.ezmeal.review.domain.model.Review;
import com.ezmeal.review.domain.repository.dto.ReviewAverageScoreDto;
import com.ezmeal.review.domain.repository.dto.ReviewSearchConditionDto;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepository {

    // 기본 crud
    Review save(Review review);
    Optional<Review> findActiveById(UUID id);
    Page<Review> searchActiveReviews(ReviewSearchConditionDto condition, Pageable pageable);

    // 멱등성 처리 (중복 생성하지 않도록)
    boolean existsActiveByUserIdAndProductId(String userId, String productId);

    // 일괄처리
    void bulkSoftDeleteByUserId(String userId, String deletedBy);
    void bulkSoftDeleteByProductId(String productId, String deletedBy);
    void bulkUpdateNicknameByUserId(String userId, String newNickname);

    // 특정 상품의 리뷰 통계 조회
    ReviewAverageScoreDto getReviewAverageScoreByProductId(String productId);

}

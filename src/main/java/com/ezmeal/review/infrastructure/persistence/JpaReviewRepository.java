package com.ezmeal.review.infrastructure.persistence;

import com.ezmeal.review.domain.model.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaReviewRepository extends JpaRepository<Review, UUID> {

    // 단건 조회
    @Query("SELECT r FROM Review r WHERE r.reviewId = :id AND r.deletedAt IS NULL")
    Optional<Review> findActiveById(@Param("id") UUID id);

    // 멱등성(중복 생성 방지)을 위한 검증
    boolean existsByUserIdAndProductIdAndDeletedAtIsNull(String userId, String productId);

    // 멱등성 검증 2 (삭제된 것도 포함)
    Optional<Review> findByUserIdAndProductId(String userId, String productId);

    // 회원 탈퇴 시 모든 리뷰 삭제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r " +
            "SET r.deletedAt = CURRENT_TIMESTAMP, r.deletedBy = :deletedBy " +
            "WHERE r.userId = :userId AND r.deletedAt IS NULL")
    void bulkSoftDeleteByUserId(@Param("userId") String userId, @Param("deletedBy") String deletedBy);

    // 상품 삭제 시 모든 리뷰 삭제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r " +
            "SET r.deletedAt = CURRENT_TIMESTAMP, r.deletedBy = :deletedBy " +
            "WHERE r.productId = :productId AND r.deletedAt IS NULL")
    void bulkSoftDeleteByProductId(@Param("productId") String productId, @Param("deletedBy") String deletedBy);

    // 사용자 닉네임 변경 시 해당 사용자의 리뷰에 있는 닉네임을 일괄 변경
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r " +
            "SET r.nickname = :newNickname " +
            "WHERE r.userId = :userId AND r.deletedAt IS NULL")
    void bulkUpdateNicknameByUserId(@Param("userId") String userId, @Param("newNickname") String newNickname);

}

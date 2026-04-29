package com.ezmeal.review.domain.model;

import com.ezmeal.common.entity.BaseEntity;
import com.ezmeal.common.enums.Role;
import com.ezmeal.common.exception.types.BadRequestException;
import com.ezmeal.common.exception.types.ForbiddenException;
import com.ezmeal.review.domain.exception.ReviewErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_review", schema = "review_db", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_user_product",
                columnNames = {"user_id", "product_id"}
        )
})
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id", length = 36, updatable = false, nullable = false)
    private UUID reviewId;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "product_id", length = 36, updatable = false, nullable = false)
    private String productId;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "contents", columnDefinition = "TEXT")
    private String contents;

    @Builder(access = AccessLevel.PRIVATE)
    private Review(String userId, String nickname, String productId, int score, String contents) {
        this.userId = userId;
        this.nickname = nickname;
        this.productId = productId;
        this.score = score;
        this.contents = contents;
    }

    // 생성
    public static Review create(String userId, String nickname, String productId, int score, String contents) {
        validateRequiredString(userId, "userId");
        validateRequiredString(nickname, "nickname");
        validateRequiredString(productId, "productId");
        validateScore(score);

        return Review.builder()
                .userId(userId)
                .nickname(nickname)
                .productId(productId)
                .score(score)
                .contents(contents)
                .build();
    }

    // 수정 (작성자이거나 관리자만 가능)
    public void updateReview(String userId, Role role, String nickname, int score, String contents) {
        validateRequiredString(nickname, "nickname");
        validateScore(score);

        if (checkRole(userId, role)) {
            this.nickname = nickname;
            this.score = score;
            this.contents = contents;
        } else {
            throw new ForbiddenException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }
    }

    // 삭제 (작성자이거나 관리자만 가능)
    public void delete(String userId, Role role) {
        if (checkRole(userId, role)) {
            super.delete(userId);
        } else {
            throw new ForbiddenException(ReviewErrorCode.REVIEW_FORBIDDEN);
        }
    }

    // 리뷰 점수 검증용 메서드 (1~5점 이내)
    private static void validateScore(int score) {
        if (score < 1 || score > 5) {
            throw new BadRequestException(ReviewErrorCode.INVALID_SCORE);
        }
    }

    // 검증 메서드
    private static void validateRequiredString(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(ReviewErrorCode.INVALID_INPUT);
        }
    }

    // 자신이 작성한 글이거나 관리자인지 검증
    boolean checkRole(String userId, Role role) {
        if (this.userId.equals(userId) || role == Role.ADMIN) {
            return true;
        } else {
            return false;
        }
    }

}

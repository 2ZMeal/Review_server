package com.ezmeal.review.infrastructure.persistence;

import static com.ezmeal.review.domain.model.QReview.review;

import com.ezmeal.review.domain.model.Review;
import com.ezmeal.review.domain.repository.dto.ReviewAverageScoreDto;
import com.ezmeal.review.domain.repository.dto.ReviewSearchConditionDto;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class QueryDslReviewRepository {

    private final JPAQueryFactory queryFactory;

    public Page<Review> searchActiveReviews(ReviewSearchConditionDto condition, Pageable pageable) {
        List<Review> content = queryFactory
                .selectFrom(review)
                .where(
                        review.deletedAt.isNull(),
                        productIdEq(condition.productId()),
                        userIdEq(condition.userId()),
                        scoreBetween(condition.minScore(), condition.maxScore()),
                        keywordContains(condition.keyword())
                )
                // 최신 순서로 정렬
                .orderBy(review.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .where(
                        review.deletedAt.isNull(),
                        productIdEq(condition.productId()),
                        userIdEq(condition.userId()),
                        scoreBetween(condition.minScore(), condition.maxScore()),
                        keywordContains(condition.keyword())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 상품의 리뷰 평균 점수
    public ReviewAverageScoreDto getReviewAverageScoreByProductId(String productId) {
        List<Tuple> result = queryFactory
                .select(
                        review.count(),
                        review.score.avg()
                )
                .from(review)
                .where(
                        review.deletedAt.isNull(),
                        review.productId.eq(productId)
                )
                .fetch();

        Tuple tuple = result.get(0);
        Long count = tuple.get(review.count());
        Double avg = tuple.get(review.score.avg());

        long totalCount = (count != null) ? count : 0L;
        double averageScore = (avg != null) ? Math.round(avg * 10) / 10.0 : 0.0;

        return new ReviewAverageScoreDto(productId, totalCount, averageScore);
    }


    /*
     *  QueryDSL 동적 쿼리용 메서드
     * */

    // 특정 상품의 리뷰만 찾기
    private BooleanExpression productIdEq(String productId) {
        return StringUtils.hasText(productId) ? review.productId.eq(productId) : null;
    }

    // 특정 사용자가 작성한 리뷰만 찾기
    private BooleanExpression userIdEq(String userId) {
        return StringUtils.hasText(userId) ? review.userId.eq(userId) : null;
    }

    // 점수별 검색 (조회)
    private BooleanExpression scoreBetween(Integer minScore, Integer maxScore) {
        // 최소 최대가 모두 존재하는 경우
        if (minScore != null && maxScore != null) {
            return review.score.between(minScore, maxScore);
        } else if (minScore != null) {
            // 최소 점수만 입력된 경우 (n점 이상)
            return review.score.goe(minScore);
        } else if (maxScore != null) {
            // 최대 점수만 입력된 경우 (n점 이하)
            return review.score.loe(maxScore);
        }
        // 최소, 최대 모두 없는 경우
        return null;
    }

    // 키워드 검색 (조회)
    private BooleanExpression keywordContains(String keyword) {
        // 값이 있다면 LIKE %keyword%를 추가하고, 없다면 null을 반환
        return StringUtils.hasText(keyword) ? review.contents.contains(keyword) : null;
    }
}

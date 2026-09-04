package com.tripfriend.domain.review.service;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.entity.TravelStyle;
import com.tripfriend.domain.place.place.entity.Category;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.review.dto.ReviewResponseDto;
import com.tripfriend.domain.review.entity.Comment;
import com.tripfriend.domain.review.entity.Review;
import com.tripfriend.domain.review.entity.ReviewViewCount;
import com.tripfriend.global.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.config.name=application-test")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({ReviewService.class, QueryDslConfig.class})
class ReviewSortingIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewService reviewService;

    private Long review01Id;
    private Long review02Id;
    private Long review03Id;
    private Long review04Id;

    @BeforeEach
    void setUp() {
        Member author = entityManager.persistAndFlush(Member.builder()
                .username("sorting-author")
                .email("sorting-author@example.test")
                .password("test-password")
                .nickname("정렬 작성자")
                .gender(Gender.UNKNOWN)
                .ageRange(AgeRange.UNKNOWN)
                .travelStyle(TravelStyle.UNKNOWN)
                .rating(0.0)
                .authority("USER")
                .verified(true)
                .build());

        Place busan = persistPlace("부산", "부산 테스트 장소");
        Place seoul = persistPlace("서울", "서울 테스트 장소");
        Place jeju = persistPlace("제주", "제주 테스트 장소");

        Review review01 = persistReview(
                "부산 바다 여행",
                busan,
                4.0,
                author,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
        Review review02 = persistReview(
                "서울 야경 추천",
                seoul,
                1.0,
                author,
                LocalDateTime.of(2026, 1, 2, 10, 0)
        );
        Review review03 = persistReview(
                "부산 맛집 후기",
                busan,
                5.0,
                author,
                LocalDateTime.of(2026, 1, 3, 10, 0)
        );
        Review review04 = persistReview(
                "제주 산책 기록",
                jeju,
                2.0,
                author,
                LocalDateTime.of(2026, 1, 4, 10, 0)
        );

        review01Id = review01.getReviewId();
        review02Id = review02.getReviewId();
        review03Id = review03.getReviewId();
        review04Id = review04.getReviewId();

        persistComments(review01, author, 1);
        persistComments(review02, author, 4);
        persistComments(review03, author, 2);
        persistComments(review04, author, 3);

        persistViewCount(review01, 30);
        persistViewCount(review02, 20);
        persistViewCount(review03, 10);
        persistViewCount(review04, 40);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("TF-TC-046 newest는 생성일 최신순으로 반환한다")
    void getReviews_sortsByNewest() {
        List<ReviewResponseDto> result = reviewService.getReviews("newest", null, null, null);

        assertReviewOrder(result, review04Id, review03Id, review02Id, review01Id);
    }

    @Test
    @DisplayName("TF-TC-046 highest_rating은 평점 높은순으로 반환한다")
    void getReviews_sortsByHighestRating() {
        List<ReviewResponseDto> result = reviewService.getReviews("highest_rating", null, null, null);

        assertReviewOrder(result, review03Id, review01Id, review04Id, review02Id);
    }

    @Test
    @DisplayName("TF-TC-046 comments는 댓글 많은순으로 반환한다")
    void getReviews_sortsByCommentCount() {
        List<ReviewResponseDto> result = reviewService.getReviews("comments", null, null, null);

        assertReviewOrder(result, review02Id, review04Id, review03Id, review01Id);
        assertThat(result)
                .extracting(ReviewResponseDto::getCommentCount)
                .containsExactly(4, 3, 2, 1);
    }

    @Test
    @DisplayName("TF-TC-046 most_viewed는 조회수 높은순으로 반환한다")
    void getReviews_sortsByViewCount() {
        List<ReviewResponseDto> result = reviewService.getReviews("most_viewed", null, null, null);

        assertReviewOrder(result, review04Id, review01Id, review02Id, review03Id);
        assertThat(result)
                .extracting(ReviewResponseDto::getViewCount)
                .containsExactly(40, 30, 20, 10);
    }

    private Place persistPlace(String cityName, String placeName) {
        return entityManager.persistAndFlush(Place.builder()
                .cityName(cityName)
                .placeName(placeName)
                .description("정렬 테스트용 장소")
                .category(Category.PLACE)
                .build());
    }

    private Review persistReview(
            String title,
            Place place,
            double rating,
            Member author,
            LocalDateTime createdAt
    ) {
        Review review = entityManager.persistAndFlush(new Review(
                title,
                "정렬 테스트용 리뷰 내용입니다.",
                rating,
                author,
                place
        ));
        review.setCreatedAt(createdAt);
        entityManager.flush();
        return review;
    }

    private void persistComments(Review review, Member author, int count) {
        for (int index = 1; index <= count; index++) {
            entityManager.persist(new Comment(
                    "정렬 테스트 댓글 " + index,
                    review,
                    author
            ));
        }
    }

    private void persistViewCount(Review review, int count) {
        ReviewViewCount viewCount = new ReviewViewCount(review);
        for (int index = 0; index < count; index++) {
            viewCount.increment();
        }
        entityManager.persist(viewCount);
    }

    private void assertReviewOrder(List<ReviewResponseDto> result, Long... expectedIds) {
        assertThat(result)
                .extracting(ReviewResponseDto::getReviewId)
                .containsExactly(expectedIds);
    }
}

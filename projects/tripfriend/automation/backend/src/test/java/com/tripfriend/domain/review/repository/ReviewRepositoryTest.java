package com.tripfriend.domain.review.repository;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.entity.TravelStyle;
import com.tripfriend.domain.place.place.entity.Category;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.review.entity.Review;
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
@Import(QueryDslConfig.class)
class ReviewRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewRepository reviewRepository;

    private Place busan;
    private Review busanOlder;
    private Review busanNewer;

    @BeforeEach
    void setUp() {
        Member author = entityManager.persistAndFlush(Member.builder()
                .username("repository-author")
                .email("repository-author@example.test")
                .password("test-password")
                .nickname("작성자 A")
                .gender(Gender.UNKNOWN)
                .ageRange(AgeRange.UNKNOWN)
                .travelStyle(TravelStyle.UNKNOWN)
                .rating(0.0)
                .authority("USER")
                .verified(true)
                .build());

        busan = entityManager.persistAndFlush(Place.builder()
                .cityName("부산")
                .placeName("부산 테스트 장소")
                .description("Repository 테스트용 부산 장소")
                .category(Category.PLACE)
                .build());
        Place seoul = entityManager.persistAndFlush(Place.builder()
                .cityName("서울")
                .placeName("서울 테스트 장소")
                .description("Repository 테스트용 서울 장소")
                .category(Category.PLACE)
                .build());
        Place jeju = entityManager.persistAndFlush(Place.builder()
                .cityName("제주")
                .placeName("제주 테스트 장소")
                .description("Repository 테스트용 제주 장소")
                .category(Category.PLACE)
                .build());

        busanOlder = persistReview(
                "부산 바다 여행",
                busan,
                4.0,
                author,
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
        persistReview(
                "서울 야경 추천",
                seoul,
                1.0,
                author,
                LocalDateTime.of(2026, 1, 2, 10, 0)
        );
        busanNewer = persistReview(
                "부산 맛집 후기",
                busan,
                5.0,
                author,
                LocalDateTime.of(2026, 1, 3, 10, 0)
        );
        persistReview(
                "제주 산책 기록",
                jeju,
                2.0,
                author,
                LocalDateTime.of(2026, 1, 4, 10, 0)
        );

        entityManager.clear();
    }

    @Test
    @DisplayName("TF-TC-044 제목 키워드 부산은 대상 리뷰만 최신순으로 반환한다")
    void findByTitleContaining_returnsMatchingReviewsNewestFirst() {
        List<Review> result = reviewRepository.findByTitleContainingOrderByCreatedAtDesc("부산");

        assertThat(result)
                .extracting(Review::getReviewId)
                .containsExactly(busanNewer.getReviewId(), busanOlder.getReviewId());
        assertThat(result)
                .extracting(Review::getTitle)
                .allMatch(title -> title.contains("부산"));
    }

    @Test
    @DisplayName("TF-TC-045 부산 장소 필터는 대상 리뷰만 최신순으로 반환한다")
    void findByPlace_returnsReviewsForPlaceNewestFirst() {
        List<Review> result = reviewRepository.findByPlace_IdOrderByCreatedAtDesc(busan.getId());

        assertThat(result)
                .extracting(Review::getReviewId)
                .containsExactly(busanNewer.getReviewId(), busanOlder.getReviewId());
        assertThat(result)
                .allMatch(review -> review.getPlace().getId().equals(busan.getId()));
    }

    @Test
    @DisplayName("TF-TC-049 존재하지 않는 제목 검색은 빈 목록을 반환한다")
    void findByTitleContaining_returnsEmptyListWhenNoReviewMatches() {
        List<Review> result = reviewRepository.findByTitleContainingOrderByCreatedAtDesc("존재하지않는제목");

        assertThat(result).isEmpty();
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
                "Repository 테스트용 리뷰 내용입니다.",
                rating,
                author,
                place
        ));
        review.setCreatedAt(createdAt);
        review.setUpdatedAt(createdAt);
        entityManager.flush();
        return review;
    }
}

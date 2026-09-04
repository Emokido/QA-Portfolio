package com.tripfriend.domain.review.controller;

import com.tripfriend.domain.member.member.entity.AgeRange;
import com.tripfriend.domain.member.member.entity.Gender;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.entity.TravelStyle;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.place.place.entity.Category;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.review.entity.Comment;
import com.tripfriend.domain.review.entity.Review;
import com.tripfriend.domain.review.entity.ReviewViewCount;
import com.tripfriend.domain.review.service.CommentService;
import com.tripfriend.domain.review.service.ReviewService;
import com.tripfriend.global.config.QueryDslConfig;
import com.tripfriend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest(properties = "spring.config.name=application-test")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({ReviewService.class, CommentService.class, QueryDslConfig.class})
class PublicReviewCommentQueryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CommentService commentService;

    private AuthService authService;
    private MockMvc reviewMockMvc;
    private MockMvc commentMockMvc;
    private Long review01Id;
    private Long review02Id;
    private Long review03Id;
    private Long review04Id;
    private List<Long> review02CommentIds;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        reviewMockMvc = MockMvcBuilders
                .standaloneSetup(new ReviewController(reviewService, authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        commentMockMvc = MockMvcBuilders
                .standaloneSetup(new CommentController(commentService, authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Member author = entityManager.persistAndFlush(Member.builder()
                .username("public-query-author")
                .email("public-query-author@example.test")
                .password("test-password")
                .nickname("공개 조회 작성자")
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

        persistComments(
                review01,
                author,
                1,
                "R-SORT-01 댓글 ",
                LocalDateTime.of(2026, 1, 1, 9, 0)
        );
        review02CommentIds = persistComments(
                review02,
                author,
                4,
                "R-SORT-02 댓글 ",
                LocalDateTime.of(2026, 1, 2, 9, 0)
        );
        persistComments(
                review03,
                author,
                2,
                "R-SORT-03 댓글 ",
                LocalDateTime.of(2026, 1, 3, 9, 0)
        );
        persistComments(
                review04,
                author,
                3,
                "R-SORT-04 댓글 ",
                LocalDateTime.of(2026, 1, 4, 9, 0)
        );

        persistViewCount(review01, 30);
        persistViewCount(review02, 20);
        persistViewCount(review03, 10);
        persistViewCount(review04, 40);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("TF-TC-001 비로그인 리뷰 목록은 전체 리뷰와 댓글 수·조회수를 반환한다")
    void getReviews_returnsAllReviewsWithCalculatedCountsWithoutAuthentication() throws Exception {
        reviewMockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-5"))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].reviewId").value(review04Id))
                .andExpect(jsonPath("$.data[0].commentCount").value(3))
                .andExpect(jsonPath("$.data[0].viewCount").value(40))
                .andExpect(jsonPath("$.data[1].reviewId").value(review03Id))
                .andExpect(jsonPath("$.data[1].commentCount").value(2))
                .andExpect(jsonPath("$.data[1].viewCount").value(10))
                .andExpect(jsonPath("$.data[2].reviewId").value(review02Id))
                .andExpect(jsonPath("$.data[2].commentCount").value(4))
                .andExpect(jsonPath("$.data[2].viewCount").value(20))
                .andExpect(jsonPath("$.data[3].reviewId").value(review01Id))
                .andExpect(jsonPath("$.data[3].commentCount").value(1))
                .andExpect(jsonPath("$.data[3].viewCount").value(30));

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("TF-TC-002 비로그인 리뷰 상세는 대상 필드와 댓글 수를 반환한다")
    void getReview_returnsTargetReviewWithoutAuthentication() throws Exception {
        reviewMockMvc.perform(get("/api/reviews/{reviewId}", review01Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-1"))
                .andExpect(jsonPath("$.data.reviewId").value(review01Id))
                .andExpect(jsonPath("$.data.title").value("부산 바다 여행"))
                .andExpect(jsonPath("$.data.rating").value(4.0))
                .andExpect(jsonPath("$.data.commentCount").value(1));

        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("TF-TC-003 비로그인 리뷰별 댓글은 생성일 오름차순으로 반환한다")
    void getCommentsByReview_returnsFourCommentsOldestFirstWithoutAuthentication() throws Exception {
        commentMockMvc.perform(get("/api/comments/review/{reviewId}", review02Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-2"))
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].commentId").value(review02CommentIds.get(0)))
                .andExpect(jsonPath("$.data[0].content").value("R-SORT-02 댓글 1"))
                .andExpect(jsonPath("$.data[1].commentId").value(review02CommentIds.get(1)))
                .andExpect(jsonPath("$.data[1].content").value("R-SORT-02 댓글 2"))
                .andExpect(jsonPath("$.data[2].commentId").value(review02CommentIds.get(2)))
                .andExpect(jsonPath("$.data[2].content").value("R-SORT-02 댓글 3"))
                .andExpect(jsonPath("$.data[3].commentId").value(review02CommentIds.get(3)))
                .andExpect(jsonPath("$.data[3].content").value("R-SORT-02 댓글 4"));

        verifyNoInteractions(authService);
    }

    private Place persistPlace(String cityName, String placeName) {
        return entityManager.persistAndFlush(Place.builder()
                .cityName(cityName)
                .placeName(placeName)
                .description("공개 조회 테스트용 장소")
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
                "공개 조회 테스트용 리뷰 내용입니다.",
                rating,
                author,
                place
        ));
        review.setCreatedAt(createdAt);
        entityManager.flush();
        return review;
    }

    private List<Long> persistComments(
            Review review,
            Member author,
            int count,
            String contentPrefix,
            LocalDateTime firstCreatedAt
    ) {
        List<Long> commentIds = new ArrayList<>();
        for (int index = 1; index <= count; index++) {
            Comment comment = entityManager.persistAndFlush(new Comment(
                    contentPrefix + index,
                    review,
                    author
            ));
            comment.setCreatedAt(firstCreatedAt.plusMinutes(index - 1L));
            entityManager.flush();
            commentIds.add(comment.getCommentId());
        }
        return commentIds;
    }

    private void persistViewCount(Review review, int count) {
        ReviewViewCount viewCount = new ReviewViewCount(review);
        for (int index = 0; index < count; index++) {
            viewCount.increment();
        }
        entityManager.persist(viewCount);
    }
}

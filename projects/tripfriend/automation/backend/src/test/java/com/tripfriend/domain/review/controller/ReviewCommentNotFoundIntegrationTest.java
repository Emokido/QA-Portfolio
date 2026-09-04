package com.tripfriend.domain.review.controller;

import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.review.repository.CommentRepository;
import com.tripfriend.domain.review.repository.ReviewRepository;
import com.tripfriend.domain.review.repository.ReviewViewCountRepository;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest(properties = "spring.config.name=application-test")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({ReviewService.class, CommentService.class, QueryDslConfig.class})
class ReviewCommentNotFoundIntegrationTest {

    private static final long MISSING_ID = 999_999L;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReviewViewCountRepository reviewViewCountRepository;

    private AuthService authService;
    private MockMvc reviewMockMvc;
    private MockMvc commentMockMvc;

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
    }

    @Test
    @DisplayName("TF-TC-047 미존재 리뷰 상세 조회는 404-1이고 리뷰 데이터가 변경되지 않는다")
    void getReview_returnsNotFoundForMissingReview() throws Exception {
        long reviewCountBefore = reviewRepository.count();
        long commentCountBefore = commentRepository.count();
        long viewCountBefore = reviewViewCountRepository.count();

        reviewMockMvc.perform(get("/api/reviews/{reviewId}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 리뷰입니다."));

        assertThat(reviewRepository.count()).isEqualTo(reviewCountBefore);
        assertThat(commentRepository.count()).isEqualTo(commentCountBefore);
        assertThat(reviewViewCountRepository.count()).isEqualTo(viewCountBefore);
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("TF-TC-048 미존재 댓글 상세 조회는 404-4이고 데이터가 변경되지 않는다")
    void getComment_returnsNotFoundForMissingComment() throws Exception {
        long reviewCountBefore = reviewRepository.count();
        long commentCountBefore = commentRepository.count();
        long viewCountBefore = reviewViewCountRepository.count();

        commentMockMvc.perform(get("/api/comments/{commentId}", MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("404-4"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 댓글입니다."));

        assertThat(reviewRepository.count()).isEqualTo(reviewCountBefore);
        assertThat(commentRepository.count()).isEqualTo(commentCountBefore);
        assertThat(reviewViewCountRepository.count()).isEqualTo(viewCountBefore);
        verifyNoInteractions(authService);
    }
}

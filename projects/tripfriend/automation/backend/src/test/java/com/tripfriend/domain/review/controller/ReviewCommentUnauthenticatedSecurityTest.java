package com.tripfriend.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.review.service.CommentService;
import com.tripfriend.domain.review.service.ReviewService;
import com.tripfriend.global.filter.DeletedMemberFilter;
import com.tripfriend.global.handler.OAuth2AuthenticationSuccessHandler;
import com.tripfriend.global.security.CustomOauth2UserService;
import com.tripfriend.global.security.CustomUserDetailsService;
import com.tripfriend.global.security.SecurityConfig;
import com.tripfriend.global.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {ReviewController.class, CommentController.class},
        properties = "spring.config.name=application-test"
)
@ActiveProfiles("test")
@Import({SecurityConfig.class, DeletedMemberFilter.class})
class ReviewCommentUnauthenticatedSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private CustomOauth2UserService customOauth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("TF-TC-020 비로그인 리뷰 생성은 401로 차단되고 서비스가 호출되지 않는다")
    void createReview_rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewRequestJson()))
                .andExpect(status().isUnauthorized());

        verifyControllerDependenciesWereNotCalled();
    }

    @Test
    @DisplayName("TF-TC-021 비로그인 리뷰 수정은 401로 차단되고 서비스가 호출되지 않는다")
    void updateReview_rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(put("/api/reviews/{reviewId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reviewRequestJson()))
                .andExpect(status().isUnauthorized());

        verifyControllerDependenciesWereNotCalled();
    }

    @Test
    @DisplayName("TF-TC-022 비로그인 리뷰 삭제는 401로 차단되고 서비스가 호출되지 않는다")
    void deleteReview_rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(delete("/api/reviews/{reviewId}", 10L))
                .andExpect(status().isUnauthorized());

        verifyControllerDependenciesWereNotCalled();
    }

    @Test
    @DisplayName("TF-TC-023 비로그인 댓글 생성은 401로 차단되고 서비스가 호출되지 않는다")
    void createComment_rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson()))
                .andExpect(status().isUnauthorized());

        verifyControllerDependenciesWereNotCalled();
    }

    @Test
    @DisplayName("TF-TC-024 비로그인 댓글 수정은 401로 차단되고 서비스가 호출되지 않는다")
    void updateComment_rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(put("/api/comments/{commentId}", 20L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson()))
                .andExpect(status().isUnauthorized());

        verifyControllerDependenciesWereNotCalled();
    }

    @Test
    @DisplayName("TF-TC-025 비로그인 댓글 삭제는 401로 차단되고 서비스가 호출되지 않는다")
    void deleteComment_rejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(delete("/api/comments/{commentId}", 20L))
                .andExpect(status().isUnauthorized());

        verifyControllerDependenciesWereNotCalled();
    }

    private String reviewRequestJson() throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("title", "유효 제목");
        request.put("content", "비로그인 보안 경계를 검증하는 리뷰 내용입니다.");
        request.put("rating", 4.0);
        request.put("placeId", 10L);
        return objectMapper.writeValueAsString(request);
    }

    private String commentRequestJson() throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("content", "유효 댓글");
        request.put("reviewId", 10L);
        return objectMapper.writeValueAsString(request);
    }

    private void verifyControllerDependenciesWereNotCalled() {
        verifyNoInteractions(authService, reviewService, commentService);
    }
}

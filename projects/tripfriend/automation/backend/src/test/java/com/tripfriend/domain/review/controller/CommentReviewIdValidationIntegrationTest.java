package com.tripfriend.domain.review.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.review.repository.CommentRepository;
import com.tripfriend.domain.review.service.CommentService;
import com.tripfriend.global.config.QueryDslConfig;
import com.tripfriend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@DataJpaTest(properties = "spring.config.name=application-test")
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({CommentService.class, QueryDslConfig.class})
class CommentReviewIdValidationIntegrationTest {

    private static final String AUTHORIZATION = "Bearer test-token";

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    private AuthService authService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CommentController(commentService, authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        Member author = Member.builder()
                .id(1L)
                .nickname("작성자 A")
                .build();
        when(authService.getLoggedInMember(AUTHORIZATION)).thenReturn(author);
    }

    @Test
    @DisplayName("TF-TC-043-A reviewId 필드 누락은 5xx 없이 거부되고 댓글이 저장되지 않는다")
    void createComment_rejectsMissingReviewIdWithoutSaving() throws Exception {
        long commentCountBefore = commentRepository.count();

        MvcResult result = performCreate(requestJson(false));

        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        assertThat(commentRepository.count()).isEqualTo(commentCountBefore);
    }

    @Test
    @DisplayName("TF-TC-043-B reviewId null은 5xx 없이 거부되고 댓글이 저장되지 않는다")
    void createComment_rejectsNullReviewIdWithoutSaving() throws Exception {
        long commentCountBefore = commentRepository.count();

        MvcResult result = performCreate(requestJson(true));

        assertThat(result.getResponse().getStatus()).isBetween(400, 499);
        assertThat(commentRepository.count()).isEqualTo(commentCountBefore);
    }

    private MvcResult performCreate(String requestJson) throws Exception {
        return mockMvc.perform(post("/api/comments")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andReturn();
    }

    private String requestJson(boolean includeNullReviewId) throws JsonProcessingException {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("content", "정상 댓글");
        if (includeNullReviewId) {
            request.put("reviewId", null);
        }
        return objectMapper.writeValueAsString(request);
    }
}

package com.tripfriend.domain.review.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.review.dto.CommentRequestDto;
import com.tripfriend.domain.review.service.CommentService;
import com.tripfriend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerTest {

    private static final String AUTHORIZATION = "Bearer test-token";

    private CommentService commentService;
    private AuthService authService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Member author;

    @BeforeEach
    void setUp() {
        commentService = mock(CommentService.class);
        authService = mock(AuthService.class);
        CommentController commentController = new CommentController(commentService, authService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        author = Member.builder()
                .id(1L)
                .nickname("작성자 A")
                .build();
    }

    @Test
    @DisplayName("TF-TC-039 댓글 1자는 400-1이고 서비스가 호출되지 않는다")
    void createComment_rejectsContentBelowMinimum() throws Exception {
        mockMvc.perform(post("/api/comments")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("다")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400-1"));

        verifyNoInteractions(authService, commentService);
    }

    @Test
    @DisplayName("TF-TC-040 댓글 2자는 201-1로 생성 요청이 처리된다")
    void createComment_acceptsContentAtMinimum() throws Exception {
        when(authService.getLoggedInMember(AUTHORIZATION)).thenReturn(author);

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("다라")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-1"));

        verify(authService).getLoggedInMember(AUTHORIZATION);
        verify(commentService).createComment(any(CommentRequestDto.class), same(author));
    }

    @Test
    @DisplayName("TF-TC-041 댓글 100자는 201-1로 생성 요청이 처리된다")
    void createComment_acceptsContentAtMaximum() throws Exception {
        when(authService.getLoggedInMember(AUTHORIZATION)).thenReturn(author);

        mockMvc.perform(post("/api/comments")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("다".repeat(100))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-1"));

        verify(authService).getLoggedInMember(AUTHORIZATION);
        verify(commentService).createComment(any(CommentRequestDto.class), same(author));
    }

    @Test
    @DisplayName("TF-TC-042 댓글 101자는 400-1이고 서비스가 호출되지 않는다")
    void createComment_rejectsContentAboveMaximum() throws Exception {
        mockMvc.perform(post("/api/comments")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("다".repeat(101))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400-1"));

        verifyNoInteractions(authService, commentService);
    }

    private String requestJson(String content) throws JsonProcessingException {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("content", content);
        request.put("reviewId", 10L);
        return objectMapper.writeValueAsString(request);
    }
}

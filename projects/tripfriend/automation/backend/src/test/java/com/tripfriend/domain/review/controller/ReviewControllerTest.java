package com.tripfriend.domain.review.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.review.dto.ReviewRequestDto;
import com.tripfriend.domain.review.service.ReviewService;
import com.tripfriend.global.aspect.ResponseAspect;
import com.tripfriend.global.filter.DeletedMemberFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReviewController.class,
        properties = "spring.config.name=application-test",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = DeletedMemberFilter.class
        )
)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@Import({ResponseAspect.class, ReviewControllerTest.AspectTestConfiguration.class})
class ReviewControllerTest {

    private static final String AUTHORIZATION = "Bearer test-token";

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private AuthService authService;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Member author;

    @BeforeEach
    void setUp() {
        author = Member.builder()
                .id(1L)
                .nickname("작성자 A")
                .build();
    }

    @TestConfiguration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    static class AspectTestConfiguration {
    }

    @Test
    @DisplayName("TF-TC-029 제목 1자는 400-1이고 서비스가 호출되지 않는다")
    void createReview_rejectsTitleBelowMinimum() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("가")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400-1"));

        verifyNoInteractions(authService, reviewService);
    }

    @Test
    @DisplayName("TF-TC-030 제목 2자는 201-1로 생성 요청이 처리된다")
    void createReview_acceptsTitleAtMinimum() throws Exception {
        String title = "가나";
        when(authService.getLoggedInMember(AUTHORIZATION)).thenReturn(author);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-1"));

        verify(authService).getLoggedInMember(AUTHORIZATION);
        verify(reviewService).createReview(any(ReviewRequestDto.class), same(author));
    }

    @Test
    @DisplayName("TF-TC-031 제목 30자는 201-1로 생성 요청이 처리된다")
    void createReview_acceptsTitleAtMaximum() throws Exception {
        String title = "가".repeat(30);
        when(authService.getLoggedInMember(AUTHORIZATION)).thenReturn(author);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-1"));

        verify(authService).getLoggedInMember(AUTHORIZATION);
        verify(reviewService).createReview(any(ReviewRequestDto.class), same(author));
    }

    @Test
    @DisplayName("TF-TC-032 제목 31자는 400-1이고 서비스가 호출되지 않는다")
    void createReview_rejectsTitleAboveMaximum() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("가".repeat(31))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400-1"));

        verifyNoInteractions(authService, reviewService);
    }

    @Test
    @DisplayName("TF-TC-033 내용 9자는 400-1이고 서비스가 호출되지 않는다")
    void content_rejectsBelowMinimum() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("유효 제목", "나".repeat(9), 4.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400-1"));

        verifyNoInteractions(authService, reviewService);
    }

    @Test
    @DisplayName("TF-TC-034 내용 10자는 201-1로 생성 요청이 처리된다")
    void content_acceptsMinimum() throws Exception {
        when(authService.getLoggedInMember(AUTHORIZATION)).thenReturn(author);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("유효 제목", "나".repeat(10), 4.0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-1"));

        verify(authService).getLoggedInMember(AUTHORIZATION);
        verify(reviewService).createReview(any(ReviewRequestDto.class), same(author));
    }

    @Test
    @DisplayName("TF-TC-035 내용 2000자는 201-1로 생성 요청이 처리된다")
    void content_acceptsMaximum() throws Exception {
        when(authService.getLoggedInMember(AUTHORIZATION)).thenReturn(author);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("유효 제목", "나".repeat(2000), 4.0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-1"));

        verify(authService).getLoggedInMember(AUTHORIZATION);
        verify(reviewService).createReview(any(ReviewRequestDto.class), same(author));
    }

    @Test
    @DisplayName("TF-TC-036 내용 2001자는 400-1이고 서비스가 호출되지 않는다")
    void content_rejectsAboveMaximum() throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("유효 제목", "나".repeat(2001), 4.0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400-1"));

        verifyNoInteractions(authService, reviewService);
    }

    @ParameterizedTest(name = "TF-TC-037 평점 {0}은 201-1로 생성 요청이 처리된다")
    @ValueSource(doubles = {1.0, 5.0})
    void rating_acceptsValidBoundaries(double rating) throws Exception {
        when(authService.getLoggedInMember(AUTHORIZATION)).thenReturn(author);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("유효 제목", "유효한 리뷰 내용입니다.", rating)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("201-1"));

        verify(authService).getLoggedInMember(AUTHORIZATION);
        verify(reviewService).createReview(any(ReviewRequestDto.class), same(author));
    }

    @ParameterizedTest(name = "TF-TC-038 평점 {0}은 400-2이고 서비스가 호출되지 않는다")
    @ValueSource(doubles = {0.0, 6.0})
    void rating_rejectsOutOfRange(double rating) throws Exception {
        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson("유효 제목", "유효한 리뷰 내용입니다.", rating)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400-2"));

        verifyNoInteractions(authService, reviewService);
    }

    private String requestJson(String title) throws JsonProcessingException {
        return requestJson(title, "경계값 테스트를 위한 리뷰 내용입니다.", 4.0);
    }

    private String requestJson(String title, String content, double rating) throws JsonProcessingException {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("title", title);
        request.put("content", content);
        request.put("rating", rating);
        request.put("placeId", 10L);
        return objectMapper.writeValueAsString(request);
    }
}

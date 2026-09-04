package com.tripfriend.domain.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.member.member.repository.MemberRepository;
import com.tripfriend.domain.member.member.service.AuthService;
import com.tripfriend.domain.review.dto.CommentRequestDto;
import com.tripfriend.domain.review.dto.CommentResponseDto;
import com.tripfriend.domain.review.service.CommentService;
import com.tripfriend.global.filter.DeletedMemberFilter;
import com.tripfriend.global.handler.OAuth2AuthenticationSuccessHandler;
import com.tripfriend.global.security.CustomOauth2UserService;
import com.tripfriend.global.security.CustomUserDetailsService;
import com.tripfriend.global.security.SecurityConfig;
import com.tripfriend.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CommentController.class,
        properties = "spring.config.name=application-test"
)
@ActiveProfiles("test")
@Import({SecurityConfig.class, DeletedMemberFilter.class, AuthService.class})
class ReviewCommentAuthenticatedSecurityTest {

    private static final String USERNAME = "security-author";
    private static final String AUTHORITY = "USER";
    private static final String VALID_TOKEN = "valid-access-token";
    private static final String INVALID_TOKEN = "invalid-access-token";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;
    private static final long COMMENT_ID = 20L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private MemberRepository memberRepository;

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

    private ValueOperations<String, String> valueOperations;
    private Member author;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        author = Member.builder()
                .id(1L)
                .username(USERNAME)
                .nickname("작성자 A")
                .authority(AUTHORITY)
                .verified(true)
                .build();
    }

    @Test
    @DisplayName("TF-TC-026 Bearer 인증은 작성자 댓글 수정에 전달된다")
    void bearerToken_authenticatesAndUpdatesOwnComment() throws Exception {
        configureValidToken(VALID_TOKEN);
        when(commentService.updateComment(eq(COMMENT_ID), any(CommentRequestDto.class), same(author)))
                .thenReturn(new CommentResponseDto());

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .header("Authorization", BEARER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-4"));

        verify(commentService).updateComment(eq(COMMENT_ID), any(CommentRequestDto.class), same(author));
    }

    @Test
    @DisplayName("TF-TC-027 accessToken 쿠키 인증은 5xx 없이 작성자 댓글 수정에 전달된다")
    void accessTokenCookie_authenticatesAndUpdatesOwnComment() throws Exception {
        configureValidToken(VALID_TOKEN);
        when(commentService.updateComment(eq(COMMENT_ID), any(CommentRequestDto.class), same(author)))
                .thenReturn(new CommentResponseDto());

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .cookie(new Cookie("accessToken", VALID_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200-4"));

        verify(commentService).updateComment(eq(COMMENT_ID), any(CommentRequestDto.class), same(author));
    }

    @Test
    @DisplayName("TF-TC-028 유효하지 않은 JWT는 401로 차단되고 변경 서비스가 호출되지 않는다")
    void invalidJwt_isRejectedBeforeCommentUpdate() throws Exception {
        when(redisTemplate.hasKey("blacklist:" + INVALID_TOKEN)).thenReturn(false);
        when(jwtUtil.getClaims(INVALID_TOKEN)).thenThrow(new JwtException("invalid test token"));

        mockMvc.perform(put("/api/comments/{commentId}", COMMENT_ID)
                        .header("Authorization", "Bearer " + INVALID_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentRequestJson()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(customUserDetailsService, memberRepository, commentService);
    }

    private void configureValidToken(String token) {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(USERNAME);
        when(claims.get("authority", String.class)).thenReturn(AUTHORITY);
        when(claims.get("verified", Boolean.class)).thenReturn(true);

        UserDetails userDetails = User.withUsername(USERNAME)
                .password("test-password")
                .roles(AUTHORITY)
                .build();

        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(false);
        when(valueOperations.get("access:" + USERNAME)).thenReturn(token);
        when(jwtUtil.getClaims(token)).thenReturn(claims);
        when(customUserDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtUtil.isDeletedAccount(token)).thenReturn(false);

        when(jwtUtil.isTokenBlacklisted(token)).thenReturn(false);
        when(jwtUtil.extractUsername(token)).thenReturn(USERNAME);
        when(jwtUtil.validateAccessTokenInRedis(USERNAME, token)).thenReturn(true);
        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.extractAuthority(token)).thenReturn(AUTHORITY);
        when(jwtUtil.extractVerified(token)).thenReturn(true);
        when(memberRepository.findByUsername(USERNAME)).thenReturn(Optional.of(author));
    }

    private String commentRequestJson() throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("content", "수정된 댓글");
        request.put("reviewId", 10L);
        return objectMapper.writeValueAsString(request);
    }
}

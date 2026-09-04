package com.tripfriend.domain.review.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.review.dto.CommentRequestDto;
import com.tripfriend.domain.review.dto.CommentResponseDto;
import com.tripfriend.domain.review.entity.Comment;
import com.tripfriend.domain.review.entity.Review;
import com.tripfriend.domain.review.repository.CommentRepository;
import com.tripfriend.domain.review.repository.ReviewRepository;
import com.tripfriend.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CommentServiceTest {

    private CommentRepository commentRepository;
    private ReviewRepository reviewRepository;
    private CommentService commentService;

    private Member author;
    private Member otherMember;
    private Review review;

    @BeforeEach
    void setUp() {
        commentRepository = mock(CommentRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        commentService = new CommentService(commentRepository, reviewRepository);

        author = Member.builder()
                .id(1L)
                .nickname("작성자 A")
                .build();
        otherMember = Member.builder()
                .id(2L)
                .nickname("비작성자 B")
                .build();
        Place place = Place.builder()
                .id(10L)
                .cityName("부산")
                .placeName("부산 테스트 장소")
                .build();
        review = new Review(
                "댓글 대상 리뷰",
                "댓글 CRUD 테스트를 위한 리뷰 내용입니다.",
                4.0,
                author,
                place
        );
        review.setReviewId(100L);
    }

    @Test
    @DisplayName("TF-TC-014 유효한 댓글을 생성하면 대상 리뷰와 작성자에 연결되어 저장된다")
    void createComment_savesCommentForReviewAndAuthor() {
        CommentRequestDto request = request("좋은 후기 감사합니다.", review.getReviewId());
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            savedComment.setCommentId(200L);
            return savedComment;
        });

        CommentResponseDto response = commentService.createComment(request, author);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getContent()).isEqualTo(request.getContent());
        assertThat(savedComment.getReview()).isSameAs(review);
        assertThat(savedComment.getMember()).isSameAs(author);
        assertThat(response.getCommentId()).isEqualTo(200L);
        assertThat(response.getReviewId()).isEqualTo(review.getReviewId());
        assertThat(response.getMemberId()).isEqualTo(author.getId());
        assertThat(response.getContent()).isEqualTo(request.getContent());
    }

    @Test
    @DisplayName("TF-TC-015 작성자는 댓글 내용을 수정하고 작성자와 리뷰 연결을 유지한다")
    void updateComment_updatesContentForAuthor() {
        Comment comment = comment(201L, "수정 전 댓글", author);
        CommentRequestDto request = request("수정된 댓글 내용", review.getReviewId());
        when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

        CommentResponseDto response = commentService.updateComment(comment.getCommentId(), request, author);

        assertThat(comment.getContent()).isEqualTo(request.getContent());
        assertThat(comment.getMember()).isSameAs(author);
        assertThat(comment.getReview()).isSameAs(review);
        assertThat(response.getCommentId()).isEqualTo(comment.getCommentId());
        assertThat(response.getContent()).isEqualTo(request.getContent());
        assertThat(response.getReviewId()).isEqualTo(review.getReviewId());
        assertThat(response.getMemberId()).isEqualTo(author.getId());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("TF-TC-016 작성자는 자신의 댓글을 삭제하고 대상 리뷰는 유지한다")
    void deleteComment_deletesOwnComment() {
        Comment comment = comment(202L, "삭제할 댓글", author);
        when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

        commentService.deleteComment(comment.getCommentId(), author);

        verify(commentRepository).delete(comment);
        assertThat(comment.getReview()).isSameAs(review);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("TF-TC-017 비작성자의 댓글 수정은 403-2로 차단되고 내용은 유지된다")
    void updateComment_rejectsNonAuthorWithoutMutation() {
        Comment comment = comment(203L, "원래 댓글 내용", author);
        CommentRequestDto request = request("권한 없는 수정", review.getReviewId());
        when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> commentService.updateComment(comment.getCommentId(), request, otherMember)
        );

        assertThat(exception.getCode()).isEqualTo("403-2");
        assertThat(comment.getContent()).isEqualTo("원래 댓글 내용");
        assertThat(comment.getMember()).isSameAs(author);
        assertThat(comment.getReview()).isSameAs(review);
        verify(commentRepository, never()).save(any(Comment.class));
        verify(commentRepository, never()).delete(any(Comment.class));
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("TF-TC-018 비작성자의 댓글 삭제는 403-2로 차단되고 댓글은 유지된다")
    void deleteComment_rejectsNonAuthorWithoutDeletion() {
        Comment comment = comment(204L, "유지할 댓글", author);
        when(commentRepository.findById(comment.getCommentId())).thenReturn(Optional.of(comment));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> commentService.deleteComment(comment.getCommentId(), otherMember)
        );

        assertThat(exception.getCode()).isEqualTo("403-2");
        assertThat(comment.getContent()).isEqualTo("유지할 댓글");
        assertThat(comment.getMember()).isSameAs(author);
        assertThat(comment.getReview()).isSameAs(review);
        verify(commentRepository, never()).delete(any(Comment.class));
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("TF-TC-019 미존재 리뷰에 댓글을 생성하면 404-3이고 댓글은 저장되지 않는다")
    void createComment_rejectsMissingReviewWithoutSave() {
        long missingReviewId = 999_999L;
        CommentRequestDto request = request("미존재 리뷰 댓글", missingReviewId);
        when(reviewRepository.findById(missingReviewId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> commentService.createComment(request, author)
        );

        assertThat(exception.getCode()).isEqualTo("404-3");
        verify(reviewRepository).findById(missingReviewId);
        verifyNoInteractions(commentRepository);
    }

    private Comment comment(Long commentId, String content, Member member) {
        Comment comment = new Comment(content, review, member);
        comment.setCommentId(commentId);
        return comment;
    }

    private CommentRequestDto request(String content, Long reviewId) {
        CommentRequestDto request = new CommentRequestDto();
        request.setContent(content);
        request.setReviewId(reviewId);
        return request;
    }
}

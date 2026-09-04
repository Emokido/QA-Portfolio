package com.tripfriend.domain.review.service;

import com.tripfriend.domain.member.member.entity.Member;
import com.tripfriend.domain.place.place.entity.Place;
import com.tripfriend.domain.place.place.repository.PlaceRepository;
import com.tripfriend.domain.review.dto.ReviewRequestDto;
import com.tripfriend.domain.review.dto.ReviewResponseDto;
import com.tripfriend.domain.review.entity.Review;
import com.tripfriend.domain.review.entity.ReviewViewCount;
import com.tripfriend.domain.review.repository.CommentRepository;
import com.tripfriend.domain.review.repository.ReviewRepository;
import com.tripfriend.domain.review.repository.ReviewViewCountRepository;
import com.tripfriend.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    private ReviewRepository reviewRepository;
    private CommentRepository commentRepository;
    private ReviewViewCountRepository viewCountRepository;
    private PlaceRepository placeRepository;
    private ReviewService reviewService;

    private Member author;
    private Member otherMember;
    private Place busan;
    private Place jeju;

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        commentRepository = mock(CommentRepository.class);
        viewCountRepository = mock(ReviewViewCountRepository.class);
        placeRepository = mock(PlaceRepository.class);
        reviewService = new ReviewService(
                reviewRepository,
                commentRepository,
                viewCountRepository,
                placeRepository
        );

        author = Member.builder()
                .id(1L)
                .nickname("작성자 A")
                .build();
        otherMember = Member.builder()
                .id(2L)
                .nickname("비작성자 B")
                .build();
        busan = Place.builder()
                .id(10L)
                .cityName("부산")
                .placeName("부산 테스트 장소")
                .build();
        jeju = Place.builder()
                .id(20L)
                .cityName("제주")
                .placeName("제주 테스트 장소")
                .build();
    }

    @Test
    @DisplayName("TF-TC-004 유효한 리뷰를 생성하면 리뷰와 조회수 0 레코드가 함께 저장된다")
    void createReview_savesReviewAndInitialViewCount() {
        ReviewRequestDto request = request(
                "서울 여행 후기",
                "서울에서 즐거운 여행을 보냈습니다.",
                4.0,
                busan.getId()
        );
        when(placeRepository.findById(busan.getId())).thenReturn(Optional.of(busan));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            savedReview.setReviewId(200L);
            return savedReview;
        });
        when(viewCountRepository.save(any(ReviewViewCount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponseDto response = reviewService.createReview(request, author);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        ArgumentCaptor<ReviewViewCount> viewCountCaptor = ArgumentCaptor.forClass(ReviewViewCount.class);
        verify(reviewRepository).save(reviewCaptor.capture());
        verify(viewCountRepository).save(viewCountCaptor.capture());

        Review savedReview = reviewCaptor.getValue();
        ReviewViewCount savedViewCount = viewCountCaptor.getValue();
        assertThat(savedReview.getTitle()).isEqualTo(request.getTitle());
        assertThat(savedReview.getContent()).isEqualTo(request.getContent());
        assertThat(savedReview.getRating()).isEqualTo(request.getRating());
        assertThat(savedReview.getMember()).isSameAs(author);
        assertThat(savedReview.getPlace()).isSameAs(busan);
        assertThat(savedViewCount.getReview()).isSameAs(savedReview);
        assertThat(savedViewCount.getCount()).isZero();
        assertThat(response.getReviewId()).isEqualTo(200L);
        assertThat(response.getPlaceId()).isEqualTo(busan.getId());
        assertThat(response.getViewCount()).isZero();
        assertThat(response.getCommentCount()).isZero();
    }

    @Test
    @DisplayName("TF-TC-005 placeId가 null이면 400-1이고 리뷰와 조회수는 저장되지 않는다")
    void createReview_rejectsNullPlaceWithoutPartialSave() {
        ReviewRequestDto request = request(
                "장소 없는 리뷰",
                "장소 ID가 없는 리뷰 생성 요청입니다.",
                4.0,
                null
        );

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reviewService.createReview(request, author)
        );

        assertThat(exception.getCode()).isEqualTo("400-1");
        verifyNoInteractions(reviewRepository, commentRepository, viewCountRepository, placeRepository);
    }

    @Test
    @DisplayName("TF-TC-006 미존재 장소이면 404-2이고 리뷰와 조회수는 저장되지 않는다")
    void createReview_rejectsMissingPlaceWithoutPartialSave() {
        long missingPlaceId = 999_999L;
        ReviewRequestDto request = request(
                "미존재 장소 리뷰",
                "존재하지 않는 장소의 리뷰 요청입니다.",
                4.0,
                missingPlaceId
        );
        when(placeRepository.findById(missingPlaceId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reviewService.createReview(request, author)
        );

        assertThat(exception.getCode()).isEqualTo("404-2");
        verify(placeRepository).findById(missingPlaceId);
        verifyNoInteractions(reviewRepository, commentRepository, viewCountRepository);
    }

    @Test
    @DisplayName("TF-TC-007 작성자는 리뷰의 제목·내용·평점을 수정할 수 있다")
    void updateReview_updatesBasicFieldsForAuthor() {
        Review review = review(100L, author, busan);
        ReviewRequestDto request = request("수정 제목", "수정된 리뷰 내용은 열 글자 이상입니다.", 5.0, busan.getId());
        stubSuccessfulUpdate(review);

        ReviewResponseDto response = reviewService.updateReview(review.getReviewId(), request, author);

        assertThat(response).isNotNull();
        assertThat(review.getTitle()).isEqualTo(request.getTitle());
        assertThat(review.getContent()).isEqualTo(request.getContent());
        assertThat(review.getRating()).isEqualTo(request.getRating());
        assertThat(review.getMember()).isSameAs(author);
        assertThat(review.getReviewId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("TF-TC-008 작성자가 여행지를 변경하면 실제 장소 연결도 변경된다")
    void updateReview_changesPlaceForAuthor() {
        Review review = review(101L, author, busan);
        ReviewRequestDto request = request("제주 여행 수정", "제주로 변경한 리뷰 내용입니다.", 4.0, jeju.getId());
        stubSuccessfulUpdate(review);
        when(placeRepository.findById(jeju.getId())).thenReturn(Optional.of(jeju));

        reviewService.updateReview(review.getReviewId(), request, author);

        assertThat(review.getPlace())
                .as("TF-REQ-004에 따라 수정 요청의 placeId가 실제 Review-Place 관계에 반영되어야 한다")
                .isSameAs(jeju);
    }

    @Test
    @DisplayName("TF-TC-009 비작성자의 리뷰 수정은 403-1로 차단되고 데이터는 유지된다")
    void updateReview_rejectsNonAuthorWithoutMutation() {
        Review review = review(102L, author, busan);
        ReviewRequestDto request = request("권한 없는 수정", "비작성자가 보낸 수정 요청입니다.", 1.0, jeju.getId());
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reviewService.updateReview(review.getReviewId(), request, otherMember)
        );

        assertThat(exception.getCode()).isEqualTo("403-1");
        assertThat(review.getTitle()).isEqualTo("기존 제목");
        assertThat(review.getContent()).isEqualTo("기존 리뷰 내용은 열 글자 이상입니다.");
        assertThat(review.getRating()).isEqualTo(4.0);
        assertThat(review.getPlace()).isSameAs(busan);
        verifyNoInteractions(commentRepository, viewCountRepository, placeRepository);
    }

    @Test
    @DisplayName("TF-TC-010 미존재 리뷰 수정은 404-1을 반환하고 신규 리뷰를 만들지 않는다")
    void updateReview_rejectsMissingReview() {
        long missingReviewId = 999_999L;
        ReviewRequestDto request = request("미존재 리뷰", "미존재 리뷰의 수정 요청입니다.", 3.0, jeju.getId());
        when(reviewRepository.findById(missingReviewId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reviewService.updateReview(missingReviewId, request, author)
        );

        assertThat(exception.getCode()).isEqualTo("404-1");
        verify(reviewRepository, never()).save(org.mockito.ArgumentMatchers.any(Review.class));
        verifyNoInteractions(commentRepository, viewCountRepository, placeRepository);
    }

    @Test
    @DisplayName("TF-TC-011 작성자가 댓글 없는 본인 리뷰를 삭제하면 조회수와 리뷰가 순서대로 삭제된다")
    void deleteReview_deletesOwnReviewAndViewCount() {
        Review review = review(103L, author, busan);
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));

        reviewService.deleteReview(review.getReviewId(), author);

        InOrder deletionOrder = inOrder(viewCountRepository, reviewRepository);
        deletionOrder.verify(viewCountRepository).deleteById(review.getReviewId());
        deletionOrder.verify(reviewRepository).delete(review);
        verifyNoInteractions(commentRepository, placeRepository);
    }

    @Test
    @DisplayName("TF-TC-012 비작성자의 리뷰 삭제는 403-1로 차단되고 삭제 작업은 발생하지 않는다")
    void deleteReview_rejectsNonAuthorWithoutDeletion() {
        Review review = review(104L, author, busan);
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reviewService.deleteReview(review.getReviewId(), otherMember)
        );

        assertThat(exception.getCode()).isEqualTo("403-1");
        verify(reviewRepository, never()).delete(any(Review.class));
        verifyNoInteractions(commentRepository, viewCountRepository, placeRepository);
    }

    @Test
    @DisplayName("TF-TC-013 미존재 리뷰 삭제는 404-1을 반환하고 삭제 작업은 발생하지 않는다")
    void deleteReview_rejectsMissingReviewWithoutDeletion() {
        long missingReviewId = 999_999L;
        when(reviewRepository.findById(missingReviewId)).thenReturn(Optional.empty());

        ServiceException exception = assertThrows(
                ServiceException.class,
                () -> reviewService.deleteReview(missingReviewId, author)
        );

        assertThat(exception.getCode()).isEqualTo("404-1");
        verify(reviewRepository, never()).delete(any(Review.class));
        verifyNoInteractions(commentRepository, viewCountRepository, placeRepository);
    }

    private void stubSuccessfulUpdate(Review review) {
        when(reviewRepository.findById(review.getReviewId())).thenReturn(Optional.of(review));
        when(commentRepository.findByReviewReviewIdOrderByCreatedAtAsc(review.getReviewId()))
                .thenReturn(List.of());
        when(viewCountRepository.findById(review.getReviewId())).thenReturn(Optional.empty());
    }

    private Review review(Long reviewId, Member member, Place place) {
        Review review = new Review(
                "기존 제목",
                "기존 리뷰 내용은 열 글자 이상입니다.",
                4.0,
                member,
                place
        );
        review.setReviewId(reviewId);
        return review;
    }

    private ReviewRequestDto request(String title, String content, double rating, Long placeId) {
        ReviewRequestDto request = new ReviewRequestDto();
        request.setTitle(title);
        request.setContent(content);
        request.setRating(rating);
        request.setPlaceId(placeId);
        return request;
    }
}

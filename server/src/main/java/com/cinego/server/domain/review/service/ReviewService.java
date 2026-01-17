package com.cinego.server.domain.review.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.exception.UnauthorizedException;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.common.util.SecurityUtil;
import com.cinego.server.domain.movie.entity.Movie;
import com.cinego.server.domain.movie.repository.MovieRepository;
import com.cinego.server.domain.review.dto.CreateReviewRequest;
import com.cinego.server.domain.review.dto.ReviewDTO;
import com.cinego.server.domain.review.dto.UpdateReviewRequest;
import com.cinego.server.domain.review.entity.Review;
import com.cinego.server.domain.review.mapper.ReviewMapper;
import com.cinego.server.domain.review.repository.ReviewRepository;
import com.cinego.server.domain.user.entity.User;
import com.cinego.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewDTO createReview(CreateReviewRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để đánh giá");
        }

        log.info("Creating review for movie: {} by user: {}", request.getMovieId(), userId);

        // Kiểm tra xem user đã review phim này chưa
        reviewRepository.findByUserIdAndMovieId(userId, request.getMovieId())
                .ifPresent(review -> {
                    throw new ConflictException("Bạn đã đánh giá phim này rồi");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));

        Review review = new Review();
        review.setUser(user);
        review.setMovie(movie);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsVerified(request.getIsVerified() != null ? request.getIsVerified() : false);
        review.setHelpfulCount(0);

        LocalDateTime now = LocalDateTime.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        Review saved = reviewRepository.save(review);
        reviewRepository.flush();

        log.info("Review created successfully with id: {}", saved.getId());
        return reviewMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getReviewsByMovie(
            UUID movieId, int page, int size, String sortBy, String sortDirection) {
        log.info("Getting reviews for movie: {}", movieId);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Review> reviewPage = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId, pageable);

        return PageUtil.toPageResponse(reviewPage.map(reviewMapper::toDTO));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getMyReviews(
            int page, int size, String sortBy, String sortDirection) {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để xem đánh giá của mình");
        }

        log.info("Getting reviews for user: {}", userId);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Review> reviewPage = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return PageUtil.toPageResponse(reviewPage.map(reviewMapper::toDTO));
    }

    @Transactional(readOnly = true)
    public ReviewDTO getReviewById(UUID id) {
        log.info("Getting review by id: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        return reviewMapper.toDTO(review);
    }

    @Transactional
    public ReviewDTO updateReview(UUID id, UpdateReviewRequest request) {
        log.info("Updating review: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập");
        }

        // Chỉ user sở hữu review hoặc admin mới được update
        // TODO: Check admin role
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền cập nhật đánh giá này");
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        // isVerified chỉ admin mới set được, bỏ qua ở đây

        review.setUpdatedAt(LocalDateTime.now());

        Review updated = reviewRepository.save(review);
        reviewRepository.flush();

        log.info("Review updated successfully: {}", id);
        return reviewMapper.toDTO(updated);
    }

    @Transactional
    public void deleteReview(UUID id) {
        log.info("Deleting review: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập");
        }

        // Chỉ user sở hữu review hoặc admin mới được xóa
        // TODO: Check admin role
        if (!review.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xóa đánh giá này");
        }

        reviewRepository.delete(review);
        reviewRepository.flush();
        log.info("Review deleted successfully: {}", id);
    }

    @Transactional
    public ReviewDTO incrementHelpfulCount(UUID id) {
        log.info("Incrementing helpful count for review: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        review.setUpdatedAt(LocalDateTime.now());

        Review updated = reviewRepository.save(review);
        reviewRepository.flush();

        return reviewMapper.toDTO(updated);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(UUID movieId) {
        Double avgRating = reviewRepository.getAverageRatingByMovieId(movieId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Transactional(readOnly = true)
    public Long getReviewCount(UUID movieId) {
        return reviewRepository.countByMovieId(movieId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewDTO> getAllReviews(
            int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all reviews with pagination");
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        return PageUtil.toPageResponse(reviewPage.map(reviewMapper::toDTO));
    }
}

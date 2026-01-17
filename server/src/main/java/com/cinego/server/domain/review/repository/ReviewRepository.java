package com.cinego.server.domain.review.repository;

import com.cinego.server.domain.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    @EntityGraph(attributePaths = {"user", "movie"})
    @Query("SELECT r FROM Review r WHERE r.movie.id = :movieId ORDER BY r.createdAt DESC")
    Page<Review> findByMovieIdOrderByCreatedAtDesc(@Param("movieId") UUID movieId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "movie"})
    @Query("SELECT r FROM Review r WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    Page<Review> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "movie"})
    Optional<Review> findByUserIdAndMovieId(UUID userId, UUID movieId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId")
    Double getAverageRatingByMovieId(@Param("movieId") UUID movieId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.movie.id = :movieId")
    Long countByMovieId(@Param("movieId") UUID movieId);

    @EntityGraph(attributePaths = {"user", "movie"})
    @Override
    Page<Review> findAll(Pageable pageable);
}

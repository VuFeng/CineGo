package com.cinego.server.domain.movie.repository;

import com.cinego.server.domain.movie.entity.Movie;
import com.cinego.server.domain.movie.entity.Movie.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    @EntityGraph(attributePaths = {"movieGenres", "movieGenres.genre"})
    @Override
    Optional<Movie> findById(UUID id);

    @EntityGraph(attributePaths = {"movieGenres", "movieGenres.genre"})
    @Override
    Page<Movie> findAll(Pageable pageable);

    Page<Movie> findByStatus(MovieStatus status, Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Movie> findByStatusAndTitleContainingIgnoreCase(MovieStatus status, String keyword, Pageable pageable);
}


package com.cinego.server.domain.movie.repository;

import com.cinego.server.domain.movie.entity.MovieGenre;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, UUID> {

    @EntityGraph(attributePaths = {"genre"})
    List<MovieGenre> findByMovieId(UUID movieId);

    @Modifying
    @Query("DELETE FROM MovieGenre mg WHERE mg.movie.id = :movieId")
    void deleteByMovieId(@Param("movieId") UUID movieId);

    boolean existsByMovieIdAndGenreId(UUID movieId, UUID genreId);
}

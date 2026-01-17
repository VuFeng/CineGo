package com.cinego.server.domain.showtime.repository;

import com.cinego.server.domain.showtime.entity.Showtime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, UUID>, JpaSpecificationExecutor<Showtime> {

    @EntityGraph(attributePaths = {"movie", "room", "room.cinema"})
    @Override
    Page<Showtime> findAll(Pageable pageable);

    List<Showtime> findByMovieId(UUID movieId);

    List<Showtime> findByRoomId(UUID roomId);

    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.status = :status")
    List<Showtime> findByMovieIdAndStatus(@Param("movieId") UUID movieId, @Param("status") Showtime.ShowtimeStatus status);

    @Query("SELECT s FROM Showtime s WHERE s.room.id = :roomId AND s.startTime >= :startTime AND s.startTime < :endTime")
    List<Showtime> findByRoomIdAndTimeRange(
            @Param("roomId") UUID roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM Showtime s WHERE s.startTime >= :startTime AND s.startTime < :endTime AND s.status = :status")
    List<Showtime> findByTimeRangeAndStatus(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") Showtime.ShowtimeStatus status);

    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.room.cinema.id = :cinemaId")
    List<Showtime> findByMovieIdAndCinemaId(@Param("movieId") UUID movieId, @Param("cinemaId") UUID cinemaId);
}

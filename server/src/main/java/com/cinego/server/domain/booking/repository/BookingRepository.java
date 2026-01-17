package com.cinego.server.domain.booking.repository;

import com.cinego.server.domain.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @EntityGraph(attributePaths = {
            "user", 
            "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema",
            "bookingSeats", "bookingSeats.seat",
            "bookingPromotions", "bookingPromotions.promotion"
    })
    @Override
    Optional<Booking> findById(UUID id);

    @EntityGraph(attributePaths = {
            "user", 
            "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema",
            "bookingSeats", "bookingSeats.seat",
            "bookingPromotions", "bookingPromotions.promotion"
    })
    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {
            "user", 
            "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema",
            "bookingSeats", "bookingSeats.seat",
            "bookingPromotions", "bookingPromotions.promotion"
    })
    Page<Booking> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "user", 
            "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema",
            "bookingSeats", "bookingSeats.seat",
            "bookingPromotions", "bookingPromotions.promotion"
    })
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT b FROM Booking b WHERE b.showtime.id = :showtimeId")
    List<Booking> findByShowtimeId(@Param("showtimeId") UUID showtimeId);

    @EntityGraph(attributePaths = {
            "user", 
            "showtime", "showtime.movie", "showtime.room", "showtime.room.cinema",
            "bookingSeats", "bookingSeats.seat",
            "bookingPromotions", "bookingPromotions.promotion"
    })
    @Override
    Page<Booking> findAll(Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.status = :status")
    List<Booking> findByStatus(@Param("status") Booking.BookingStatus status);
}

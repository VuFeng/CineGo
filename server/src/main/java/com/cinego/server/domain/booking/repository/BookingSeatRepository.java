package com.cinego.server.domain.booking.repository;

import com.cinego.server.domain.booking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, UUID> {

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.showtime.id = :showtimeId AND bs.status != 'CANCELLED'")
    List<BookingSeat> findActiveSeatsByShowtimeId(@Param("showtimeId") UUID showtimeId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.seat.id = :seatId AND bs.showtime.id = :showtimeId AND bs.status != 'CANCELLED'")
    List<BookingSeat> findActiveSeatsBySeatIdAndShowtimeId(
            @Param("seatId") UUID seatId,
            @Param("showtimeId") UUID showtimeId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.booking.id = :bookingId")
    List<BookingSeat> findByBookingId(@Param("bookingId") UUID bookingId);

    @Query("SELECT bs FROM BookingSeat bs WHERE bs.status = 'HOLD' AND bs.holdExpiresAt < :now")
    List<BookingSeat> findExpiredHolds(@Param("now") LocalDateTime now);
}

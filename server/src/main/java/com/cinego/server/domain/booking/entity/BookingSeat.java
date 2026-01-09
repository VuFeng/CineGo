package com.cinego.server.domain.booking.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.seat.entity.Seat;
import com.cinego.server.domain.showtime.entity.Showtime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "booking_seats",
        indexes = {
                @Index(
                        name = "uk_booking_seat_showtime_seat",
                        columnList = "showtime_id, seat_id",
                        unique = true
                ),
                @Index(name = "idx_booking_seat_booking", columnList = "booking_id"),
                @Index(name = "idx_booking_seat_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private BookingSeatStatus status = BookingSeatStatus.HOLD;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    public enum BookingSeatStatus {
        HOLD,
        BOOKED,
        CANCELLED
    }
}


package com.cinego.server.domain.showtime.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.booking.entity.Booking;
import com.cinego.server.domain.booking.entity.BookingSeat;
import com.cinego.server.domain.movie.entity.Movie;
import com.cinego.server.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "showtimes",
        indexes = {
                @Index(name = "idx_showtime_movie", columnList = "movie_id"),
                @Index(name = "idx_showtime_room", columnList = "room_id"),
                @Index(name = "idx_showtime_start_time", columnList = "start_time"),
                @Index(name = "idx_showtime_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Showtime extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ShowtimeStatus status = ShowtimeStatus.ACTIVE;

    @Column(name = "available_seats")
    private Integer availableSeats;

    /**
     * Định dạng chiếu: 2D, 3D, IMAX, v.v.
     * Lưu dưới dạng string để linh hoạt.
     */
    @Column(name = "format", length = 32)
    private String format;

    public enum ShowtimeStatus {
        ACTIVE,
        CANCELLED,
        COMPLETED
    }
}


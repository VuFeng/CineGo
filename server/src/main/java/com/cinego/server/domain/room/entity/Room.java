package com.cinego.server.domain.room.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.cinema.entity.Cinema;
import com.cinego.server.domain.seat.entity.Seat;
import com.cinego.server.domain.showtime.entity.Showtime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Showtime> showtimes = new ArrayList<>();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "seat_layout", columnDefinition = "text")
    private String seatLayout;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", length = 16)
    private RoomType roomType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public enum RoomType {
        TWO_D,
        THREE_D,
        IMAX,
        VIP
    }
}
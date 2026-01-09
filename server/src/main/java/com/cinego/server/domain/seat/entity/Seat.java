package com.cinego.server.domain.seat.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.booking.entity.BookingSeat;
import com.cinego.server.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seat_room_row_number",
                        columnNames = {"room_id", "row", "number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @Column(name = "row", nullable = false, length = 8)
    private String row;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", length = 16)
    private SeatType seatType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public enum SeatType {
        NORMAL,
        VIP,
        COUPLE,
        DISABLED
    }
}
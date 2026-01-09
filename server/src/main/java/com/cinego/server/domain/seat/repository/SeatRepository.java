package com.cinego.server.domain.seat.repository;

import com.cinego.server.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query("SELECT s FROM Seat s WHERE s.room.id = :roomId")
    List<Seat> findByRoomId(@Param("roomId") UUID roomId);

    @Query("SELECT COUNT(s) > 0 FROM Seat s WHERE s.room.id = :roomId AND s.row = :row AND s.number = :number")
    boolean existsByRoomIdAndRowAndNumber(@Param("roomId") UUID roomId, @Param("row") String row, @Param("number") Integer number);
}


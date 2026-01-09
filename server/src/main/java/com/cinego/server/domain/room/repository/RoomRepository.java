package com.cinego.server.domain.room.repository;

import com.cinego.server.domain.cinema.entity.Cinema;
import com.cinego.server.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByCinema(Cinema cinema);
}


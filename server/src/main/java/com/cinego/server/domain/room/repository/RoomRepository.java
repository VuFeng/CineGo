package com.cinego.server.domain.room.repository;

import com.cinego.server.domain.cinema.entity.Cinema;
import com.cinego.server.domain.room.entity.Room;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    @EntityGraph(attributePaths = {"showtimes"})
    @Override
    Optional<Room> findById(UUID id);

    List<Room> findByCinema(Cinema cinema);
}


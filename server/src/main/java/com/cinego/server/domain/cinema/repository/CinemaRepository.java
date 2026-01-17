package com.cinego.server.domain.cinema.repository;

import com.cinego.server.domain.cinema.entity.Cinema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, UUID> {

    @EntityGraph(attributePaths = {"rooms"})
    @Override
    Optional<Cinema> findById(UUID id);

    Page<Cinema> findByCityIgnoreCase(String city, Pageable pageable);

    Page<Cinema> findByCityIgnoreCaseAndDistrictIgnoreCase(String city, String district, Pageable pageable);
}


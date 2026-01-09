package com.cinego.server.domain.cinema.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "cinemas",
        indexes = {
                @Index(name = "idx_cinema_city", columnList = "city"),
                @Index(name = "idx_cinema_district", columnList = "district"),
                @Index(name = "idx_cinema_is_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cinema extends BaseEntity {

    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false, columnDefinition = "text")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "opening_hours", columnDefinition = "text")
    private String openingHours;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
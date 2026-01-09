package com.cinego.server.domain.booking.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.promotion.entity.Promotion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "booking_promotions",
        indexes = {
                @Index(name = "idx_booking_promotion_booking", columnList = "booking_id"),
                @Index(name = "idx_booking_promotion_promotion", columnList = "promotion_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingPromotion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;
}

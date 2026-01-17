package com.cinego.server.domain.payment.entity;

import com.cinego.server.common.entity.BaseEntity;
import com.cinego.server.domain.booking.entity.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payment_booking", columnList = "booking_id"),
                @Index(name = "idx_payment_transaction_id", columnList = "transaction_id", unique = true),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_provider", columnList = "payment_provider")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 32)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", length = 32)
    private PaymentProvider paymentProvider;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "payment_url", columnDefinition = "text")
    private String paymentUrl;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "refund_reason", columnDefinition = "text")
    private String refundReason;

    @Column(name = "metadata", columnDefinition = "jsonb")
    @org.hibernate.annotations.ColumnTransformer(
        read = "metadata::text",
        write = "CAST(? AS jsonb)"
    )
    private String metadata;

    public enum PaymentMethod {
        CASH,
        CARD,
        MOMO,
        ZALOPAY
    }

    public enum PaymentProvider {
        VNPAY,
        MOMO,
        ZALOPAY,
        CASH
    }

    public enum PaymentStatus {
        PENDING,
        SUCCESS,
        FAILED,
        REFUNDED
    }
}

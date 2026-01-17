package com.cinego.server.domain.payment.repository;

import com.cinego.server.domain.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @EntityGraph(attributePaths = {"booking"})
    @Override
    Optional<Payment> findById(UUID id);

    @EntityGraph(attributePaths = {"booking"})
    @Override
    Page<Payment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"booking"})
    Optional<Payment> findByTransactionId(String transactionId);

    @EntityGraph(attributePaths = {"booking"})
    List<Payment> findByBookingId(UUID bookingId);

    @EntityGraph(attributePaths = {"booking"})
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId ORDER BY p.createdAt DESC")
    List<Payment> findByBookingIdOrderByCreatedAtDesc(@Param("bookingId") UUID bookingId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findByStatus(@Param("status") Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.paymentProvider = :provider AND p.status = :status")
    List<Payment> findByProviderAndStatus(
            @Param("provider") Payment.PaymentProvider provider,
            @Param("status") Payment.PaymentStatus status);
}

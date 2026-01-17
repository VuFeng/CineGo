package com.cinego.server.domain.payment.service;

import com.cinego.server.common.exception.BadRequestException;
import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.exception.UnauthorizedException;
import com.cinego.server.common.util.SecurityUtil;
import com.cinego.server.domain.booking.entity.Booking;
import com.cinego.server.domain.booking.repository.BookingRepository;
import com.cinego.server.domain.payment.dto.CreatePaymentRequest;
import com.cinego.server.domain.payment.dto.PaymentDTO;
import com.cinego.server.domain.payment.dto.UpdatePaymentRequest;
import com.cinego.server.domain.payment.entity.Payment;
import com.cinego.server.domain.payment.mapper.PaymentMapper;
import com.cinego.server.domain.payment.repository.PaymentRepository;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;

    public PaymentDTO createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for booking: {}", request.getBookingId());

        // Lấy current user
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để thanh toán");
        }

        // Load booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", request.getBookingId()));

        // Check authorization
        if (!booking.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Bạn không có quyền thanh toán booking này");
        }

        // Validate booking status
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BadRequestException("Không thể thanh toán cho booking đã bị hủy");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BadRequestException("Booking đã hoàn thành");
        }

        // Check nếu đã có payment thành công
        List<Payment> existingPayments = paymentRepository.findByBookingId(request.getBookingId());
        boolean hasSuccessPayment = existingPayments.stream()
                .anyMatch(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS);
        if (hasSuccessPayment) {
            throw new ConflictException("Booking đã được thanh toán thành công");
        }

        // Validate payment method
        Payment.PaymentMethod paymentMethod;
        try {
            paymentMethod = Payment.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Phương thức thanh toán không hợp lệ: " + request.getPaymentMethod());
        }

        // Determine payment provider
        Payment.PaymentProvider paymentProvider = determinePaymentProvider(paymentMethod, request.getPaymentProvider());

        // Generate transaction ID
        String transactionId = generateTransactionId();

        // Tạo payment
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentProvider(paymentProvider);
        payment.setTransactionId(transactionId);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        // Generate payment URL nếu cần (cho online payment)
        if (paymentProvider != Payment.PaymentProvider.CASH) {
            // TODO: Integrate with payment gateway (VNPay, MoMo, ZaloPay)
            // payment.setPaymentUrl(generatePaymentUrl(transactionId, booking.getTotalPrice()));
        }

        LocalDateTime now = LocalDateTime.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking payment info
        booking.setPaymentId(savedPayment.getId());
        booking.setPaymentMethod(booking.getPaymentMethod() != null ? 
                booking.getPaymentMethod() : Booking.PaymentMethod.valueOf(paymentMethod.name()));
        bookingRepository.save(booking);

        paymentRepository.flush();
        bookingRepository.flush();

        log.info("Payment created successfully with id: {} and transactionId: {}", 
                savedPayment.getId(), transactionId);

        return paymentMapper.toDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentById(UUID id) {
        log.info("Getting payment by id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !payment.getBooking().getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xem payment này");
        }

        return paymentMapper.toDTO(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByTransactionId(String transactionId) {
        log.info("Getting payment by transactionId: {}", transactionId);
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !payment.getBooking().getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xem payment này");
        }

        return paymentMapper.toDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsByBookingId(UUID bookingId) {
        log.info("Getting payments by booking id: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !booking.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xem payments của booking này");
        }

        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        return payments.stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentDTO> getAllPayments(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all payments with pagination");
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Payment> paymentPage = paymentRepository.findAll(pageable);
        
        List<PaymentDTO> content = paymentPage.getContent().stream()
                .map(paymentMapper::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<PaymentDTO>builder()
                .content(content)
                .page(paymentPage.getNumber())
                .size(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .first(paymentPage.isFirst())
                .last(paymentPage.isLast())
                .build();
    }

    public PaymentDTO updatePayment(UUID id, UpdatePaymentRequest request) {
        log.info("Updating payment with id: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !payment.getBooking().getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền cập nhật payment này");
        }

        // Cập nhật status
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                Payment.PaymentStatus newStatus = Payment.PaymentStatus.valueOf(request.getStatus().toUpperCase());
                
                // Validate status transition
                if (payment.getStatus() == Payment.PaymentStatus.SUCCESS && 
                    newStatus != Payment.PaymentStatus.REFUNDED) {
                    throw new BadRequestException("Payment đã thành công, chỉ có thể refund");
                }

                payment.setStatus(newStatus);

                if (newStatus == Payment.PaymentStatus.SUCCESS) {
                    payment.setPaidAt(LocalDateTime.now());
                    // Update booking status
                    Booking booking = payment.getBooking();
                    booking.setPaymentStatus(Booking.PaymentStatus.PAID);
                    booking.setStatus(Booking.BookingStatus.CONFIRMED);
                    // Update booking seats status
                    booking.getBookingSeats().forEach(bs -> {
                        bs.setStatus(com.cinego.server.domain.booking.entity.BookingSeat.BookingSeatStatus.BOOKED);
                    });
                    bookingRepository.save(booking);
                } else if (newStatus == Payment.PaymentStatus.REFUNDED) {
                    payment.setRefundedAt(LocalDateTime.now());
                    if (request.getRefundReason() != null) {
                        payment.setRefundReason(request.getRefundReason());
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Trạng thái thanh toán không hợp lệ: " + request.getStatus());
            }
        }

        // Cập nhật transaction ID
        if (request.getTransactionId() != null && !request.getTransactionId().isBlank()) {
            if (paymentRepository.findByTransactionId(request.getTransactionId()).isPresent() &&
                !payment.getTransactionId().equals(request.getTransactionId())) {
                throw new ConflictException("Transaction ID đã tồn tại");
            }
            payment.setTransactionId(request.getTransactionId());
        }

        // Cập nhật payment URL
        if (request.getPaymentUrl() != null) {
            payment.setPaymentUrl(request.getPaymentUrl());
        }

        // Cập nhật refund reason
        if (request.getRefundReason() != null) {
            payment.setRefundReason(request.getRefundReason());
        }

        payment.setUpdatedAt(LocalDateTime.now());
        Payment updatedPayment = paymentRepository.save(payment);
        paymentRepository.flush();

        log.info("Payment updated successfully with id: {}", updatedPayment.getId());
        return paymentMapper.toDTO(updatedPayment);
    }

    /**
     * Xác định payment provider từ payment method
     */
    private Payment.PaymentProvider determinePaymentProvider(
            Payment.PaymentMethod paymentMethod, String providerString) {
        
        if (providerString != null && !providerString.isBlank()) {
            try {
                return Payment.PaymentProvider.valueOf(providerString.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Fall through to default
            }
        }

        // Default mapping
        return switch (paymentMethod) {
            case CASH -> Payment.PaymentProvider.CASH;
            case CARD -> Payment.PaymentProvider.VNPAY;
            case MOMO -> Payment.PaymentProvider.MOMO;
            case ZALOPAY -> Payment.PaymentProvider.ZALOPAY;
        };
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        String transactionId;
        int attempts = 0;
        do {
            // Format: PAY-YYYYMMDDHHMMSS-XXXXXX
            String timestamp = LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase().replace("-", "");
            transactionId = "PAY-" + timestamp + "-" + random;
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Không thể tạo transaction ID duy nhất");
            }
        } while (paymentRepository.findByTransactionId(transactionId).isPresent());

        return transactionId;
    }

    /**
     * Mô phỏng thanh toán thành công (cho testing/demo)
     */
    public PaymentDTO simulatePaymentSuccess(UUID paymentId) {
        log.info("Simulating payment success for payment: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !payment.getBooking().getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền thực hiện thanh toán này");
        }

        // Validate payment status
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new BadRequestException("Payment đã được thanh toán thành công");
        }

        if (payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new BadRequestException("Payment đã được refund, không thể thanh toán lại");
        }

        // Update payment status to SUCCESS
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        // Update booking status
        Booking booking = payment.getBooking();
        booking.setPaymentStatus(Booking.PaymentStatus.PAID);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        
        // Update booking seats status
        booking.getBookingSeats().forEach(bs -> {
            bs.setStatus(com.cinego.server.domain.booking.entity.BookingSeat.BookingSeatStatus.BOOKED);
        });

        Payment savedPayment = paymentRepository.save(payment);
        bookingRepository.save(booking);
        paymentRepository.flush();
        bookingRepository.flush();

        log.info("Payment simulated successfully for payment: {}", paymentId);
        return paymentMapper.toDTO(savedPayment);
    }
}

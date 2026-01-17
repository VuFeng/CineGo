package com.cinego.server.domain.booking.service;

import com.cinego.server.common.exception.BadRequestException;
import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.exception.UnauthorizedException;
import com.cinego.server.common.util.SecurityUtil;
import com.cinego.server.domain.booking.dto.*;
import com.cinego.server.domain.booking.entity.Booking;
import com.cinego.server.domain.booking.entity.BookingPromotion;
import com.cinego.server.domain.booking.entity.BookingSeat;
import com.cinego.server.domain.booking.mapper.BookingMapper;
import com.cinego.server.domain.booking.repository.BookingRepository;
import com.cinego.server.domain.booking.repository.BookingSeatRepository;
import com.cinego.server.domain.promotion.entity.Promotion;
import com.cinego.server.domain.promotion.repository.PromotionRepository;
import com.cinego.server.domain.seat.entity.Seat;
import com.cinego.server.domain.seat.repository.SeatRepository;
import com.cinego.server.domain.showtime.entity.Showtime;
import com.cinego.server.domain.showtime.repository.ShowtimeRepository;
import com.cinego.server.domain.user.entity.User;
import com.cinego.server.domain.user.repository.UserRepository;
import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.util.PageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final PromotionRepository promotionRepository;
    private final BookingMapper bookingMapper;

    private static final int HOLD_DURATION_MINUTES = 15; // Thời gian giữ ghế

    public BookingDTO createBooking(CreateBookingRequest request) {
        log.info("Creating booking for showtime: {} with {} seats", request.getShowtimeId(), request.getSeatIds().size());

        // Lấy current user
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để đặt vé");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Load showtime
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Showtime", "id", request.getShowtimeId()));

        // Validate showtime
        if (showtime.getStatus() != Showtime.ShowtimeStatus.ACTIVE) {
            throw new BadRequestException("Suất chiếu không còn hoạt động");
        }

        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Không thể đặt vé cho suất chiếu đã qua");
        }

        // Load và validate seats
        List<Seat> seats = seatRepository.findAllById(request.getSeatIds());
        if (seats.size() != request.getSeatIds().size()) {
            throw new BadRequestException("Một số ghế không tồn tại");
        }

        // Validate seats thuộc cùng room với showtime
        UUID showtimeRoomId = showtime.getRoom().getId();
        for (Seat seat : seats) {
            if (!seat.getRoom().getId().equals(showtimeRoomId)) {
                throw new BadRequestException("Ghế không thuộc phòng chiếu này");
            }
            if (!seat.getIsActive()) {
                throw new BadRequestException("Ghế không hoạt động");
            }
        }

        // Check seat availability
        List<BookingSeat> activeBookingSeats = bookingSeatRepository.findActiveSeatsByShowtimeId(request.getShowtimeId());
        Set<UUID> bookedSeatIds = activeBookingSeats.stream()
                .map(bs -> bs.getSeat().getId())
                .collect(Collectors.toSet());

        for (Seat seat : seats) {
            if (bookedSeatIds.contains(seat.getId())) {
                throw new ConflictException("Ghế " + seat.getRow() + seat.getNumber() + " đã được đặt");
            }
        }

        // Tính toán giá
        BigDecimal basePrice = showtime.getPrice();
        BigDecimal totalPrice = basePrice.multiply(BigDecimal.valueOf(seats.size()));

        // Áp dụng promotions
        List<BookingPromotion> bookingPromotions = new ArrayList<>();
        BigDecimal totalDiscount = BigDecimal.ZERO;

        if (request.getPromotionCodes() != null && !request.getPromotionCodes().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (String code : request.getPromotionCodes()) {
                Promotion promotion = promotionRepository.findValidPromotionByCode(code, now)
                        .orElseThrow(() -> new BadRequestException("Mã khuyến mãi không hợp lệ: " + code));

                // Validate promotion có áp dụng cho movie này không
                if (promotion.getApplicableMovies() != null && promotion.getApplicableMovies().length > 0) {
                    boolean applicable = Arrays.asList(promotion.getApplicableMovies())
                            .contains(showtime.getMovie().getId());
                    if (!applicable) {
                        throw new BadRequestException("Mã khuyến mãi không áp dụng cho phim này");
                    }
                }

                // Validate min purchase amount
                if (promotion.getMinPurchaseAmount() != null &&
                    totalPrice.compareTo(promotion.getMinPurchaseAmount()) < 0) {
                    throw new BadRequestException("Giá trị đơn hàng chưa đạt mức tối thiểu để áp dụng mã khuyến mãi");
                }

                // Tính discount
                BigDecimal discount = calculateDiscount(promotion, totalPrice);
                totalDiscount = totalDiscount.add(discount);

                // Tạo BookingPromotion
                BookingPromotion bookingPromotion = new BookingPromotion();
                bookingPromotion.setPromotion(promotion);
                bookingPromotion.setDiscountAmount(discount);
                bookingPromotions.add(bookingPromotion);
            }
        }

        // Tính tổng giá sau discount
        BigDecimal finalPrice = totalPrice.subtract(totalDiscount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }

        // Generate booking code
        String bookingCode = generateBookingCode();

        // Tạo booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setTotalPrice(finalPrice);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setBookingCode(bookingCode);

        LocalDateTime now = LocalDateTime.now();
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);

        Booking savedBooking = bookingRepository.save(booking);

        // Tạo booking seats
        LocalDateTime holdExpiresAt = now.plusMinutes(HOLD_DURATION_MINUTES);
        List<BookingSeat> bookingSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(savedBooking);
            bookingSeat.setShowtime(showtime);
            bookingSeat.setSeat(seat);
            bookingSeat.setStatus(BookingSeat.BookingSeatStatus.HOLD);
            bookingSeat.setHoldExpiresAt(holdExpiresAt);
            bookingSeat.setPrice(basePrice);
            bookingSeat.setCreatedAt(now);
            bookingSeat.setUpdatedAt(now);
            bookingSeats.add(bookingSeat);
        }
        bookingSeatRepository.saveAll(bookingSeats);

        // Link promotions với booking
        for (BookingPromotion bp : bookingPromotions) {
            bp.setBooking(savedBooking);
            bp.setCreatedAt(now);
            bp.setUpdatedAt(now);
        }
        savedBooking.setBookingPromotions(bookingPromotions);
        bookingRepository.save(savedBooking);

        // Update promotion used count
        for (BookingPromotion bp : bookingPromotions) {
            Promotion p = bp.getPromotion();
            p.setUsedCount(p.getUsedCount() + 1);
            promotionRepository.save(p);
        }

        bookingRepository.flush();
        bookingSeatRepository.flush();

        log.info("Booking created successfully with id: {} and code: {}", savedBooking.getId(), bookingCode);

        // Reload để có đầy đủ relationships
        Booking fullBooking = bookingRepository.findById(savedBooking.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", savedBooking.getId()));

        return bookingMapper.toDTO(fullBooking);
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingById(UUID id) {
        log.info("Getting booking by id: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !booking.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xem booking này");
        }

        return bookingMapper.toDTO(booking);
    }

    @Transactional(readOnly = true)
    public BookingDTO getBookingByCode(String bookingCode) {
        log.info("Getting booking by code: {}", bookingCode);
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "bookingCode", bookingCode));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !booking.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xem booking này");
        }

        return bookingMapper.toDTO(booking);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingDTO> getMyBookings(int page, int size, String sortBy, String sortDirection) {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để xem danh sách đặt vé");
        }

        log.info("Getting bookings for user: {} with pagination", userId);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Booking> bookingPage = bookingRepository.findByUserId(userId, pageable);
        
        List<BookingDTO> content = bookingPage.getContent().stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<BookingDTO>builder()
                .content(content)
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .first(bookingPage.isFirst())
                .last(bookingPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingDTO> getAllBookings(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all bookings with pagination");
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);
        
        List<BookingDTO> content = bookingPage.getContent().stream()
                .map(bookingMapper::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.<BookingDTO>builder()
                .content(content)
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .first(bookingPage.isFirst())
                .last(bookingPage.isLast())
                .build();
    }

    public BookingDTO updateBooking(UUID id, UpdateBookingRequest request) {
        log.info("Updating booking with id: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !booking.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền cập nhật booking này");
        }

        // Cập nhật status
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                Booking.BookingStatus status = Booking.BookingStatus.valueOf(request.getStatus().toUpperCase());
                booking.setStatus(status);

                if (status == Booking.BookingStatus.CANCELLED) {
                    booking.setCancelledAt(LocalDateTime.now());
                    booking.setCancelledBy(currentUserId);
                    // Release seats
                    for (BookingSeat bs : booking.getBookingSeats()) {
                        bs.setStatus(BookingSeat.BookingSeatStatus.CANCELLED);
                    }
                }
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Trạng thái không hợp lệ: " + request.getStatus());
            }
        }

        // Cập nhật payment status
        if (request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank()) {
            try {
                booking.setPaymentStatus(Booking.PaymentStatus.valueOf(request.getPaymentStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Trạng thái thanh toán không hợp lệ: " + request.getPaymentStatus());
            }
        }

        // Cập nhật payment method
        if (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank()) {
            try {
                booking.setPaymentMethod(Booking.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Phương thức thanh toán không hợp lệ: " + request.getPaymentMethod());
            }
        }

        booking.setUpdatedAt(LocalDateTime.now());
        Booking updatedBooking = bookingRepository.save(booking);
        bookingRepository.flush();

        log.info("Booking updated successfully with id: {}", updatedBooking.getId());
        return bookingMapper.toDTO(updatedBooking);
    }

    public void cancelBooking(UUID id) {
        log.info("Cancelling booking with id: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !booking.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền hủy booking này");
        }

        // Validate có thể hủy không
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking đã bị hủy");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BadRequestException("Không thể hủy booking đã hoàn thành");
        }

        // Hủy booking
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledBy(currentUserId);

        // Release seats
        for (BookingSeat bs : booking.getBookingSeats()) {
            bs.setStatus(BookingSeat.BookingSeatStatus.CANCELLED);
        }

        booking.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
        bookingRepository.flush();

        log.info("Booking cancelled successfully with id: {}", id);
    }

    /**
     * Tính toán discount từ promotion
     */
    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal totalPrice) {
        BigDecimal discount = BigDecimal.ZERO;

        if (promotion.getDiscountType() == Promotion.DiscountType.PERCENTAGE) {
            discount = totalPrice.multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = promotion.getDiscountValue();
        }

        // Áp dụng max discount nếu có
        if (promotion.getMaxDiscountAmount() != null &&
            discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            discount = promotion.getMaxDiscountAmount();
        }

        return discount;
    }

    /**
     * Generate unique booking code
     */
    private String generateBookingCode() {
        String code;
        int attempts = 0;
        do {
            // Format: CINEGO-YYYYMMDD-XXXXXX (6 random alphanumeric)
            String date = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase().replace("-", "");
            code = "CINEGO-" + date + "-" + random;
            attempts++;
            if (attempts > 10) {
                throw new RuntimeException("Không thể tạo mã booking duy nhất");
            }
        } while (bookingRepository.findByBookingCode(code).isPresent());

        return code;
    }
}

package com.cinego.server.domain.booking.mapper;

import com.cinego.server.domain.booking.dto.BookingDTO;
import com.cinego.server.domain.booking.dto.BookingPromotionDTO;
import com.cinego.server.domain.booking.dto.BookingSeatDTO;
import com.cinego.server.domain.booking.entity.Booking;
import com.cinego.server.domain.booking.entity.BookingPromotion;
import com.cinego.server.domain.booking.entity.BookingSeat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "showtimeId", source = "showtime.id")
    @Mapping(target = "movieTitle", source = "showtime.movie.title")
    @Mapping(target = "showtimeStartTime", source = "showtime.startTime")
    @Mapping(target = "cinemaName", source = "showtime.room.cinema.name")
    @Mapping(target = "roomName", source = "showtime.room.name")
    @Mapping(target = "status", source = "status", defaultValue = "PENDING")
    @Mapping(target = "paymentStatus", source = "paymentStatus", defaultValue = "PENDING")
    @Mapping(target = "seats", source = "bookingSeats")
    @Mapping(target = "promotions", source = "bookingPromotions")
    BookingDTO toDTO(Booking booking);

    @Mapping(target = "seatId", source = "seat.id")
    @Mapping(target = "seatRow", source = "seat.row")
    @Mapping(target = "seatNumber", source = "seat.number")
    @Mapping(target = "seatType", source = "seat.seatType", defaultValue = "NORMAL")
    @Mapping(target = "status", source = "status", defaultValue = "HOLD")
    BookingSeatDTO toSeatDTO(BookingSeat bookingSeat);

    List<BookingSeatDTO> toSeatDTOList(List<BookingSeat> bookingSeats);

    @Mapping(target = "promotionId", source = "promotion.id")
    @Mapping(target = "promotionCode", source = "promotion.code")
    @Mapping(target = "promotionName", source = "promotion.name")
    BookingPromotionDTO toPromotionDTO(BookingPromotion bookingPromotion);

    List<BookingPromotionDTO> toPromotionDTOList(List<BookingPromotion> bookingPromotions);
}

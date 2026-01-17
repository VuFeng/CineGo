package com.cinego.server.domain.payment.mapper;

import com.cinego.server.domain.payment.dto.PaymentDTO;
import com.cinego.server.domain.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "bookingCode", source = "booking.bookingCode")
    @Mapping(target = "status", source = "status", defaultValue = "PENDING")
    PaymentDTO toDTO(Payment payment);
}

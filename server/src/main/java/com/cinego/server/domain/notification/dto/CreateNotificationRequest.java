package com.cinego.server.domain.notification.dto;

import com.cinego.server.domain.notification.entity.Notification.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotNull(message = "User ID không được để trống")
    private UUID userId;

    @NotNull(message = "Loại thông báo không được để trống")
    private NotificationType type;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String message;

    private UUID relatedId; // ID liên quan (booking_id, payment_id, etc.)
}

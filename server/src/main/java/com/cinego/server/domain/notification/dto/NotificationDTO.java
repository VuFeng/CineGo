package com.cinego.server.domain.notification.dto;

import com.cinego.server.domain.notification.entity.Notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private UUID relatedId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

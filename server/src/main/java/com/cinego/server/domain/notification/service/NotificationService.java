package com.cinego.server.domain.notification.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.exception.UnauthorizedException;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.common.util.SecurityUtil;
import com.cinego.server.domain.notification.dto.CreateNotificationRequest;
import com.cinego.server.domain.notification.dto.NotificationDTO;
import com.cinego.server.domain.notification.entity.Notification;
import com.cinego.server.domain.notification.mapper.NotificationMapper;
import com.cinego.server.domain.notification.repository.NotificationRepository;
import com.cinego.server.domain.user.entity.User;
import com.cinego.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationDTO createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(request.getType());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setIsRead(false);
        notification.setRelatedId(request.getRelatedId());

        LocalDateTime now = LocalDateTime.now();
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);

        Notification saved = notificationRepository.save(notification);
        notificationRepository.flush();

        log.info("Notification created successfully with id: {}", saved.getId());
        return notificationMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getMyNotifications(
            int page, int size, String sortBy, String sortDirection, Boolean isRead) {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để xem thông báo");
        }

        log.info("Getting notifications for user: {}, isRead: {}", userId, isRead);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);

        Page<Notification> notificationPage;
        if (isRead != null) {
            notificationPage = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(
                    userId, isRead, pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return PageUtil.toPageResponse(notificationPage.map(notificationMapper::toDTO));
    }

    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(UUID id) {
        log.info("Getting notification by id: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !notification.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xem thông báo này");
        }

        return notificationMapper.toDTO(notification);
    }

    @Transactional
    public NotificationDTO markAsRead(UUID id) {
        log.info("Marking notification as read: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !notification.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền cập nhật thông báo này");
        }

        notification.setIsRead(true);
        notification.setUpdatedAt(LocalDateTime.now());

        Notification updated = notificationRepository.save(notification);
        notificationRepository.flush();

        log.info("Notification marked as read: {}", id);
        return notificationMapper.toDTO(updated);
    }

    @Transactional
    public void markAllAsRead() {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập");
        }

        log.info("Marking all notifications as read for user: {}", userId);
        Pageable pageable = PageUtil.createPageable(0, 1000, "createdAt", "DESC");
        Page<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable);

        LocalDateTime now = LocalDateTime.now();
        unreadNotifications.getContent().forEach(notification -> {
            notification.setIsRead(true);
            notification.setUpdatedAt(now);
        });

        notificationRepository.saveAll(unreadNotifications.getContent());
        notificationRepository.flush();

        log.info("Marked {} notifications as read", unreadNotifications.getContent().size());
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount() {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập");
        }

        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDTO> getAllNotifications(
            int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all notifications with pagination");
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<Notification> notificationPage = notificationRepository.findAll(pageable);

        return PageUtil.toPageResponse(notificationPage.map(notificationMapper::toDTO));
    }

    @Transactional
    public void deleteNotification(UUID id) {
        log.info("Deleting notification: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        // Check authorization
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId == null || !notification.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("Bạn không có quyền xóa thông báo này");
        }

        notificationRepository.delete(notification);
        notificationRepository.flush();
        log.info("Notification deleted successfully: {}", id);
    }
}

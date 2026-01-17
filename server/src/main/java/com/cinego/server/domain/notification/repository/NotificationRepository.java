package com.cinego.server.domain.notification.repository;

import com.cinego.server.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = :isRead ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(
            @Param("userId") UUID userId, 
            @Param("isRead") Boolean isRead, 
            Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = {"user"})
    @Override
    Page<Notification> findAll(Pageable pageable);
}

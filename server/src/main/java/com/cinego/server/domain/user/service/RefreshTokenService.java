package com.cinego.server.domain.user.service;

import com.cinego.server.common.exception.BadRequestException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.security.JwtUtil;
import com.cinego.server.domain.user.entity.RefreshToken;
import com.cinego.server.domain.user.entity.User;
import com.cinego.server.domain.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * Tạo và lưu refresh token mới vào database
     */
    public RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        log.info("Creating refresh token for user: {}", user.getId());

        // Generate JWT refresh token
        String token = jwtUtil.generateRefreshToken(user.getId());

        // Tính toán expiry date
        LocalDateTime expiresAt = LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration));

        // Tạo RefreshToken entity
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setIsRevoked(false);
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);
        
        // Set createdAt và updatedAt
        LocalDateTime now = LocalDateTime.now();
        refreshToken.setCreatedAt(now);
        refreshToken.setUpdatedAt(now);

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        refreshTokenRepository.flush();
        
        log.info("Refresh token created successfully with id: {}", savedToken.getId());
        return savedToken;
    }

    /**
     * Tìm refresh token theo token string
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Validate refresh token từ database
     */
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        log.info("Validating refresh token");

        // Validate JWT token trước
        if (!jwtUtil.validateRefreshToken(token)) {
            throw new BadRequestException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // Tìm token trong database
        RefreshToken refreshToken = refreshTokenRepository.findValidToken(token, LocalDateTime.now())
                .orElseThrow(() -> new BadRequestException("Refresh token không tồn tại hoặc đã bị thu hồi"));

        // Kiểm tra user có active không
        if (!refreshToken.getUser().getIsActive()) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        log.info("Refresh token validated successfully for user: {}", refreshToken.getUser().getId());
        return refreshToken;
    }

    /**
     * Revoke một refresh token cụ thể
     */
    public void revokeRefreshToken(String token) {
        log.info("Revoking refresh token");
        
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("RefreshToken", "token", token));
        
        // Kiểm tra xem token đã bị revoke chưa
        if (refreshToken.getIsRevoked()) {
            log.warn("Refresh token already revoked: {}", token);
            throw new BadRequestException("Refresh token đã bị thu hồi");
        }
        
        // Kiểm tra xem token đã hết hạn chưa
        if (refreshToken.isExpired()) {
            log.warn("Refresh token already expired: {}", token);
            throw new BadRequestException("Refresh token đã hết hạn");
        }
        
        refreshToken.revoke();
        refreshToken.setUpdatedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        refreshTokenRepository.flush();
        
        log.info("Refresh token revoked successfully");
    }

    /**
     * Revoke tất cả refresh tokens của một user (dùng khi logout hoặc đổi password)
     */
    public void revokeAllUserTokens(UUID userId) {
        log.info("Revoking all refresh tokens for user: {}", userId);
        
        int revokedCount = refreshTokenRepository.revokeAllUserTokens(userId, LocalDateTime.now());
        refreshTokenRepository.flush();
        
        log.info("Revoked {} refresh tokens for user: {}", revokedCount, userId);
    }

    /**
     * Xóa refresh token cũ khi tạo token mới (rotation strategy)
     */
    public void deleteOldRefreshToken(String oldToken) {
        log.info("Deleting old refresh token");
        
        refreshTokenRepository.findByToken(oldToken)
                .ifPresent(refreshTokenRepository::delete);
        refreshTokenRepository.flush();
    }

    /**
     * Cleanup expired tokens (có thể gọi từ scheduled task)
     */
    @Transactional
    public int cleanupExpiredTokens() {
        log.info("Cleaning up expired refresh tokens");
        
        int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        refreshTokenRepository.flush();
        
        log.info("Deleted {} expired refresh tokens", deletedCount);
        return deletedCount;
    }
}

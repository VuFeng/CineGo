package com.cinego.server.domain.user.service;

import com.cinego.server.common.dto.PageResponse;
import com.cinego.server.common.exception.BadRequestException;
import com.cinego.server.common.exception.ConflictException;
import com.cinego.server.common.exception.ResourceNotFoundException;
import com.cinego.server.common.security.JwtUtil;
import com.cinego.server.common.util.PageUtil;
import com.cinego.server.domain.user.dto.*;
import com.cinego.server.domain.user.entity.User;
import com.cinego.server.domain.user.mapper.UserMapper;
import com.cinego.server.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public UserDTO createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        // Kiểm tra email đã tồn tại (trước khi vào transaction)
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email đã được sử dụng");
        }

        // Kiểm tra phone đã tồn tại (nếu có)
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new ConflictException("Số điện thoại đã được sử dụng");
            }
        }

        // Tạo user mới (trong transaction)
        return createUserTransaction(request);
    }

    @Transactional
    private UserDTO createUserTransaction(CreateUserRequest request) {
        // Tạo user mới
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setRole(User.UserRole.USER);
        user.setIsActive(true);
        user.setEmailVerified(false);
        
        // Set createdAt và updatedAt thủ công vì Hibernate @CreationTimestamp 
        // có thể không hoạt động đúng với Supabase pooler
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        userRepository.flush(); // Flush để đảm bảo entity được persist
        log.info("User created successfully with id: {}", savedUser.getId());

        return userMapper.toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id) {
        log.info("Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return userMapper.toDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return userMapper.toDTO(user);
    }

    public UserDTO updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Cập nhật thông tin
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            // Kiểm tra phone đã tồn tại (nếu thay đổi)
            if (!request.getPhone().equals(user.getPhone())) {
                if (userRepository.existsByPhone(request.getPhone())) {
                    throw new ConflictException("Số điện thoại đã được sử dụng");
                }
                user.setPhone(request.getPhone());
            }
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        // Set updatedAt thủ công vì Hibernate @UpdateTimestamp 
        // có thể không hoạt động đúng với Supabase pooler
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        userRepository.flush(); // Flush để đảm bảo entity được persist
        log.info("User updated successfully with id: {}", updatedUser.getId());

        return userMapper.toDTO(updatedUser);
    }

    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Soft delete - set isActive = false và updatedAt
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        userRepository.flush(); // Flush để đảm bảo entity được persist
        log.info("User deactivated successfully with id: {}", id);
    }

    public LoginResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        log.info("Login attempt for: {}", request.getEmailOrPhone());

        // Tìm user theo email hoặc phone
        User user = userRepository.findByEmailOrPhone(request.getEmailOrPhone())
                .orElseThrow(() -> new BadRequestException("Email/Số điện thoại hoặc mật khẩu không đúng"));

        // Kiểm tra tài khoản có active không
        if (!user.getIsActive()) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Email/Số điện thoại hoặc mật khẩu không đúng");
        }

        // Cập nhật last login và updatedAt
        LocalDateTime now = LocalDateTime.now();
        user.setLastLogin(now);
        user.setUpdatedAt(now);
        userRepository.save(user);
        userRepository.flush(); // Flush để đảm bảo entity được persist

        // Generate access token
        String accessToken = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // Tạo và lưu refresh token vào database
        com.cinego.server.domain.user.entity.RefreshToken refreshTokenEntity = 
                refreshTokenService.createRefreshToken(user, deviceInfo, ipAddress);
        String refreshToken = refreshTokenEntity.getToken();

        UserDTO userDTO = userMapper.toDTO(user);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    public LoginResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        // Validate refresh token từ database
        com.cinego.server.domain.user.entity.RefreshToken refreshTokenEntity = 
                refreshTokenService.validateRefreshToken(refreshToken);

        User user = refreshTokenEntity.getUser();

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // Token rotation: Xóa token cũ và tạo token mới
        refreshTokenService.deleteOldRefreshToken(refreshToken);
        com.cinego.server.domain.user.entity.RefreshToken newRefreshTokenEntity = 
                refreshTokenService.createRefreshToken(user, refreshTokenEntity.getDeviceInfo(), refreshTokenEntity.getIpAddress());
        String newRefreshToken = newRefreshTokenEntity.getToken();

        UserDTO userDTO = userMapper.toDTO(user);
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userDTO)
                .build();
    }

    /**
     * Logout - revoke refresh token
     */
    public void logout(String refreshToken) {
        log.info("Logging out user");
        refreshTokenService.revokeRefreshToken(refreshToken);
    }

    /**
     * Logout từ tất cả devices - revoke tất cả refresh tokens của user
     */
    public void logoutAll(UUID userId) {
        log.info("Logging out user from all devices: {}", userId);
        refreshTokenService.revokeAllUserTokens(userId);
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(UUID userId) {
        return getUserById(userId);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getAllUsers(int page, int size, String sortBy, String sortDirection) {
        log.info("Getting all users with pagination - page: {}, size: {}", page, size);
        Pageable pageable = PageUtil.createPageable(page, size, sortBy, sortDirection);
        Page<User> userPage = userRepository.findAll(pageable);
        return PageUtil.toPageResponse(userPage.map(userMapper::toDTO));
    }
}

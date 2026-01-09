package com.cinego.server.domain.user.controller;

import com.cinego.server.common.dto.ApiResponse;
import com.cinego.server.common.exception.UnauthorizedException;
import com.cinego.server.common.util.SecurityUtil;
import com.cinego.server.domain.user.dto.*;
import com.cinego.server.domain.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated  // Enable validation for path variables and request params
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký thành công", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = userService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @PathVariable @NotNull(message = "User ID không được để trống") UUID id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UUID userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Bạn cần đăng nhập để truy cập endpoint này");
        }
        UserDTO user = userService.getCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable @NotNull(message = "User ID không được để trống") UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin thành công", user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable @NotNull(message = "User ID không được để trống") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa tài khoản thành công", null));
    }
}

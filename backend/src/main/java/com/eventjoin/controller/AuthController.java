package com.eventjoin.controller;

import com.eventjoin.common.ApiResponse;
import com.eventjoin.dto.LoginRequest;
import com.eventjoin.dto.LoginResponse;
import com.eventjoin.dto.RegisterRequest;
import com.eventjoin.dto.UserDTO;
import com.eventjoin.entity.User;
import com.eventjoin.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }
    
    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ApiResponse.success("注册成功", convertToDTO(user));
    }
    
    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ApiResponse.success(convertToDTO(user));
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}

package com.eventjoin.dto;

import com.eventjoin.enums.UserRole;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    
    public LoginResponse(String token, Long id, String username, String email, UserRole role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}

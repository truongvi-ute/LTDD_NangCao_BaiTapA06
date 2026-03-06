package com.mapic.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String name;
    private String avatarUrl;
    
    public AuthResponse(String token, Long userId, String username, String email, String name) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.name = name;
    }
    
    public AuthResponse(String token, Long userId, String username, String email, String name, String avatarUrl) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }
}

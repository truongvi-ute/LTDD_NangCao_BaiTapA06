package com.mapic.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "Username không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username không hợp lệ")
    private String username;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}

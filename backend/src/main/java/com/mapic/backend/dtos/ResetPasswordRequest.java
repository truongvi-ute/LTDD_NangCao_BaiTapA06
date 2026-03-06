package com.mapic.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Mã OTP không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP phải là 6 chữ số")
    private String otp;
    
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String newPassword;
}

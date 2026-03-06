package com.mapic.backend.controllers;

import com.mapic.backend.dtos.*;
import com.mapic.backend.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMessage));
        }
        
        try {
            authService.register(request, httpRequest);
            return ResponseEntity.ok(
                    ApiResponse.success("Đăng ký thành công. Vui lòng kiểm tra email để xác thực tài khoản")
            );
        } catch (Exception e) {
            log.error("Registration error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/verify-registration")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyRegistration(
            @Valid @RequestBody VerifyOtpRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMessage));
        }
        
        try {
            AuthResponse authResponse = authService.verifyRegistration(request.getEmail(), request.getCode());
            return ResponseEntity.ok(
                    ApiResponse.success("Xác thực thành công", authResponse)
            );
        } catch (Exception e) {
            log.error("Verification error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMessage));
        }
        
        try {
            authService.forgotPassword(request.getEmail(), httpRequest);
            return ResponseEntity.ok(
                    ApiResponse.success("Mã OTP đã được gửi đến email của bạn")
            );
        } catch (Exception e) {
            log.error("Forgot password error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMessage));
        }
        
        try {
            authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ResponseEntity.ok(
                    ApiResponse.success("Mật khẩu đã được đặt lại thành công")
            );
        } catch (Exception e) {
            log.error("Reset password error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        try {
            authService.changePassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ResponseEntity.ok(new ApiResponse<>(true, "Đổi mật khẩu thành công", null));
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @PostMapping("/request-change-password")
    public ResponseEntity<ApiResponse<Void>> requestChangePassword(
            @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        try {
            authService.requestChangePassword(request.getEmail(), httpRequest);
            return ResponseEntity.ok(new ApiResponse<>(true, "Mã OTP đã được gửi đến email của bạn", null));
        } catch (Exception e) {
            log.error("Error requesting change password", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest) {
        
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(errorMessage));
        }
        
        try {
            authService.resendOtp(request.getEmail(), request.getType(), httpRequest);
            return ResponseEntity.ok(
                    ApiResponse.success("Mã OTP mới đã được gửi")
            );
        } catch (Exception e) {
            log.error("Resend OTP error: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError().getDefaultMessage();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(errorMessage));
        }
        
        try {
            AuthResponse authResponse = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(
                    ApiResponse.success("Đăng nhập thành công", authResponse)
            );
        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}

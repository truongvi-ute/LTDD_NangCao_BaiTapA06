package com.mapic.backend.services;

import com.mapic.backend.dtos.AuthResponse;
import com.mapic.backend.dtos.RegisterRequest;
import com.mapic.backend.entities.AccountStatus;
import com.mapic.backend.entities.OtpType;
import com.mapic.backend.entities.User;
import com.mapic.backend.repositories.UserRepository;
import com.mapic.backend.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Transactional
    public void register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Validate username format
        if (!request.getUsername().matches("^[a-zA-Z0-9_]+$")) {
            throw new RuntimeException("Username chỉ được chứa chữ cái, số và dấu gạch dưới");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        
        // Create temporary user (not verified yet)
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(AccountStatus.INACTIVE); // Will be ACTIVE after OTP verification
        user.setIsVerified(false);
        user.setIsBlocked(false);
        
        user = userRepository.save(user);
        
        // Send OTP
        otpService.createOtp(request.getEmail(), OtpType.REGISTRATION, user, httpRequest);
        
        log.info("User registered successfully: {}", request.getUsername());
    }
    
    @Transactional
    public AuthResponse verifyRegistration(String email, String code) {
        // Verify OTP
        otpService.verifyOtp(email, code, OtpType.REGISTRATION);
        
        // Find user and activate
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        user.setIsVerified(true);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        
        // Generate JWT token
        String token = jwtUtil.generateTokenWithRole(user.getUsername(), user.getId(), "USER");
        
        // Get avatar URL if exists
        String avatarUrl = user.getProfile() != null ? UserService.buildAvatarUrl(user.getProfile().getAvatarUrl()) : null;
        
        log.info("Registration verified successfully for: {}", email);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getName(), avatarUrl);
    }
    
    @Transactional
    public void forgotPassword(String email, HttpServletRequest httpRequest) {
        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));
        
        // Check if user is verified
        if (!user.getIsVerified()) {
            throw new RuntimeException("Tài khoản chưa được xác thực");
        }
        
        // Send OTP
        otpService.createOtp(email, OtpType.FORGOT_PASSWORD, user, httpRequest);
        
        log.info("Forgot password OTP sent to: {}", email);
    }
    
    @Transactional
    public void changePassword(String email, String otp, String newPassword) {
        // Verify OTP
        otpService.verifyOtp(email, otp, OtpType.CHANGE_PASSWORD);
        
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for: {}", email);
    }
    
    @Transactional
    public void requestChangePassword(String email, HttpServletRequest httpRequest) {
        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống"));
        
        // Check if user is verified
        if (!user.getIsVerified()) {
            throw new RuntimeException("Tài khoản chưa được xác thực");
        }
        
        // Send OTP
        otpService.createOtp(email, OtpType.CHANGE_PASSWORD, user, httpRequest);
        
        log.info("Change password OTP sent to: {}", email);
    }
    
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        // Verify OTP
        otpService.verifyOtp(email, otp, OtpType.FORGOT_PASSWORD);
        
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password reset successfully for: {}", email);
    }
    
    @Transactional
    public void resendOtp(String email, String typeStr, HttpServletRequest httpRequest) {
        // Parse OTP type
        OtpType type;
        try {
            if ("registration".equalsIgnoreCase(typeStr)) {
                type = OtpType.REGISTRATION;
            } else if ("forgot-password".equalsIgnoreCase(typeStr)) {
                type = OtpType.FORGOT_PASSWORD;
            } else if ("change-password".equalsIgnoreCase(typeStr)) {
                type = OtpType.CHANGE_PASSWORD;
            } else {
                throw new RuntimeException("Loại OTP không hợp lệ");
            }
        } catch (Exception e) {
            throw new RuntimeException("Loại OTP không hợp lệ");
        }
        
        // Find user
        User user = userRepository.findByEmail(email).orElse(null);
        
        // For registration, user might not exist yet or not verified
        if (type == OtpType.REGISTRATION) {
            if (user == null) {
                throw new RuntimeException("Vui lòng đăng ký lại");
            }
            if (user.getIsVerified()) {
                throw new RuntimeException("Tài khoản đã được xác thực");
            }
        }
        
        // For forgot password, user must exist and be verified
        if (type == OtpType.FORGOT_PASSWORD) {
            if (user == null) {
                throw new RuntimeException("Email không tồn tại trong hệ thống");
            }
            if (!user.getIsVerified()) {
                throw new RuntimeException("Tài khoản chưa được xác thực");
            }
        }
        
        // For change password, user must exist and be verified
        if (type == OtpType.CHANGE_PASSWORD) {
            if (user == null) {
                throw new RuntimeException("Email không tồn tại trong hệ thống");
            }
            if (!user.getIsVerified()) {
                throw new RuntimeException("Tài khoản chưa được xác thực");
            }
        }
        
        // Create new OTP
        otpService.createOtp(email, type, user, httpRequest);
        
        log.info("OTP resent for email: {} with type: {}", email, type);
    }
    
    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username hoặc mật khẩu không đúng"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Username hoặc mật khẩu không đúng");
        }
        
        if (!user.getIsVerified()) {
            throw new RuntimeException("Tài khoản chưa được xác thực. Vui lòng kiểm tra email");
        }
        
        if (user.getIsBlocked()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }
        
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản không hoạt động");
        }
        
        String token = jwtUtil.generateTokenWithRole(user.getUsername(), user.getId(), "USER");
        
        log.info("Generated token for user {}: {}", username, token.substring(0, Math.min(50, token.length())) + "...");
        
        // Get avatar URL if exists
        String avatarUrl = user.getProfile() != null ? UserService.buildAvatarUrl(user.getProfile().getAvatarUrl()) : null;
        
        log.info("User logged in successfully: {}", username);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getName(), avatarUrl);
    }
    
    // private String generateUsername(String email) {
    //     return email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
    // }
}

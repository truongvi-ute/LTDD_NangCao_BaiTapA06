package com.mapic.backend.services;

import com.mapic.backend.entities.OtpToken;
import com.mapic.backend.entities.OtpType;
import com.mapic.backend.entities.User;
import com.mapic.backend.repositories.OtpTokenRepository;
import com.mapic.backend.utils.OtpGenerator;
import com.mapic.backend.utils.OtpSender;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {
    
    private final OtpTokenRepository otpTokenRepository;
    private final OtpGenerator otpGenerator;
    private final OtpSender otpSender;
    
    private static final int MAX_OTP_PER_15_MINUTES = 3;
    private static final int MAX_ATTEMPTS = 5;
    
    @Transactional
    public OtpToken createOtp(String email, OtpType type, User user, HttpServletRequest request) {
        // Check rate limiting
        LocalDateTime since = LocalDateTime.now().minusMinutes(15);
        long recentCount = otpTokenRepository.countRecentOtpByEmailAndType(email, type, since);
        
        if (recentCount >= MAX_OTP_PER_15_MINUTES) {
            throw new RuntimeException("Bạn đã yêu cầu quá nhiều mã OTP. Vui lòng thử lại sau 15 phút");
        }
        
        // Invalidate old OTPs
        otpTokenRepository.invalidateOldOtps(email, type);
        
        // Generate new OTP
        String code = otpGenerator.generateOtp();
        String ipAddress = getClientIp(request);
        
        OtpToken otpToken = new OtpToken();
        otpToken.setCode(code);
        otpToken.setEmail(email);
        otpToken.setType(type);
        otpToken.setUser(user);
        otpToken.setIpAddress(ipAddress);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(type.getValidityMinutes()));
        
        otpToken = otpTokenRepository.save(otpToken);
        
        // Send OTP via console (or email/SMS in production)
        otpSender.sendOtp(email, code, type);
        
        log.info("OTP created for email: {} with type: {}", email, type);
        return otpToken;
    }
    
    @Transactional
    public OtpToken verifyOtp(String email, String code, OtpType type) {
        OtpToken otpToken = otpTokenRepository.findByEmailAndCodeAndType(email, code, type)
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ"));
        
        // Check if already used
        if (otpToken.getIsUsed()) {
            throw new RuntimeException("Mã OTP đã được sử dụng");
        }
        
        // Check if expired
        if (otpToken.isExpired()) {
            throw new RuntimeException("Mã OTP đã hết hạn");
        }
        
        // Check max attempts
        if (otpToken.isMaxAttemptsExceeded()) {
            throw new RuntimeException("Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu mã OTP mới");
        }
        
        // Mark as used
        otpToken.markAsUsed();
        otpTokenRepository.save(otpToken);
        
        log.info("OTP verified successfully for email: {}", email);
        return otpToken;
    }
    
    @Transactional
    public void incrementAttempt(String email, String code, OtpType type) {
        otpTokenRepository.findByEmailAndCodeAndType(email, code, type)
                .ifPresent(otpToken -> {
                    otpToken.incrementAttempt();
                    otpTokenRepository.save(otpToken);
                });
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

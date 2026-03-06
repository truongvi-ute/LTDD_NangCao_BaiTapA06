package com.mapic.backend.services;

import com.mapic.backend.entities.OtpType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    
    public void sendOtpEmail(String email, String code, OtpType type) {
        // This method is deprecated, use OtpSender instead
        log.warn("EmailService.sendOtpEmail is deprecated. Use OtpSender instead.");
    }
}

package com.mapic.backend.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class OtpGenerator {
    
    private static final SecureRandom random = new SecureRandom();
    
    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}

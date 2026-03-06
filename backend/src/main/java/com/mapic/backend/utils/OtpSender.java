package com.mapic.backend.utils;

import com.mapic.backend.entities.OtpType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OTP Sender Utility
 * Hiện tại gửi qua console, có thể mở rộng để gửi qua email hoặc SMS
 */
@Component
@Slf4j
public class OtpSender {
    
    /**
     * Send OTP via console (for development)
     * TODO: Implement email/SMS sending for production
     */
    public void sendOtp(String recipient, String code, OtpType type) {
        sendViaConsole(recipient, code, type);
        
        // TODO: Uncomment when ready for production
        // if (isEmail(recipient)) {
        //     sendViaEmail(recipient, code, type);
        // } else if (isPhoneNumber(recipient)) {
        //     sendViaSms(recipient, code, type);
        // }
    }
    
    /**
     * Send OTP via console (Development mode)
     */
    private void sendViaConsole(String recipient, String code, OtpType type) {
        log.info("========================================");
        log.info("OTP CODE: {}", code);
        log.info("Email: {}", recipient);
        log.info("Type: {}", type.name());
        log.info("Valid: {} minutes", type.getValidityMinutes());
        log.info("========================================");
    }
    
    /**
     * Send OTP via Email (Production mode)
     * TODO: Implement with JavaMailSender or SendGrid
     */
    private void sendViaEmail(String email, String code, OtpType type) {
        log.info("Sending OTP via Email to: {}", email);
        
        // Example implementation:
        // String subject = "Mã xác thực OTP - " + type.getDescription();
        // String body = buildEmailTemplate(code, type);
        // mailSender.send(email, subject, body);
        
        throw new UnsupportedOperationException("Email sending not implemented yet");
    }
    
    /**
     * Send OTP via SMS (Production mode)
     * TODO: Implement with Twilio or other SMS provider
     */
    private void sendViaSms(String phoneNumber, String code, OtpType type) {
        log.info("Sending OTP via SMS to: {}", phoneNumber);
        
        // Example implementation:
        // String message = String.format("Mã OTP của bạn là: %s. Có hiệu lực trong %d phút.",
        //                                code, type.getValidityMinutes());
        // smsProvider.send(phoneNumber, message);
        
        throw new UnsupportedOperationException("SMS sending not implemented yet");
    }
    
    /**
     * Build HTML email template for OTP
     */
    private String buildEmailTemplate(String code, OtpType type) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .otp-box { background: #f5f5f5; padding: 20px; text-align: center; border-radius: 8px; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #007AFF; letter-spacing: 8px; }
                    .footer { margin-top: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Xác thực tài khoản MAPIC</h2>
                    <p>Bạn đã yêu cầu mã OTP cho: <strong>%s</strong></p>
                    <div class="otp-box">
                        <p>Mã OTP của bạn là:</p>
                        <div class="otp-code">%s</div>
                        <p>Mã có hiệu lực trong %d phút</p>
                    </div>
                    <div class="footer">
                        <p>Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>
                        <p>&copy; 2024 MAPIC. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, type.getDescription(), code, type.getValidityMinutes());
    }
    
    /**
     * Check if recipient is email format
     */
    private boolean isEmail(String recipient) {
        return recipient != null && recipient.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
    
    /**
     * Check if recipient is phone number format (Vietnamese)
     */
    private boolean isPhoneNumber(String recipient) {
        return recipient != null && recipient.matches("^0[0-9]{9}$");
    }
}

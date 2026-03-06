package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens", indexes = {
    @Index(name = "idx_email_type", columnList = "email, type"),
    @Index(name = "idx_expiry", columnList = "expiresAt"),
    @Index(name = "idx_user_created", columnList = "user_id, createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 6)
    private String code;
    
    @Column(nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpType type;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private Boolean isUsed = false;
    
    @Column(nullable = false)
    private Integer attemptCount = 0;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime usedAt;
    
    @Column(length = 45)
    private String ipAddress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null && type != null) {
            expiresAt = LocalDateTime.now().plusMinutes(type.getValidityMinutes());
        }
    }
    
    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Check if OTP is valid (not used, not expired, attempts not exceeded)
     */
    public boolean isValid() {
        return !isUsed && !isExpired() && attemptCount < 5;
    }
    
    /**
     * Increment attempt count
     */
    public void incrementAttempt() {
        this.attemptCount++;
    }
    
    /**
     * Mark OTP as used
     */
    public void markAsUsed() {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
    }
    
    /**
     * Check if max attempts exceeded
     */
    public boolean isMaxAttemptsExceeded() {
        return attemptCount >= 5;
    }
}

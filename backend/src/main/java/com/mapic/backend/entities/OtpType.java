package com.mapic.backend.entities;

public enum OtpType {
    REGISTRATION("Đăng ký tài khoản", 10),
    FORGOT_PASSWORD("Quên mật khẩu", 10),
    CHANGE_PASSWORD("Thay đổi mật khẩu", 5);
    
    private final String description;
    private final int validityMinutes;
    
    OtpType(String description, int validityMinutes) {
        this.description = description;
        this.validityMinutes = validityMinutes;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getValidityMinutes() {
        return validityMinutes;
    }
}

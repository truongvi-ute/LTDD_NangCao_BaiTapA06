package com.mapic.backend.entities;

public enum ReportStatus {
    PENDING,    // Đang chờ xử lý
    REVIEWING,  // Đang xem xét
    RESOLVED,   // Đã xử lý (moment bị block)
    DISMISSED   // Bỏ qua (không vi phạm)
}

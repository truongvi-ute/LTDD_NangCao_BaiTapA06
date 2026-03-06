package com.mapic.backend.entities;

public enum MomentStatus {
    ACTIVE,         // Đang hoạt động
    BLOCKED,        // Bị chặn bởi admin
    DELETED,        // Đã xóa bởi user
    PENDING_REVIEW  // Đang chờ review (sau khi bị report)
}

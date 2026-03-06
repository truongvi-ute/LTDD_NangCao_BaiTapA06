package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admins")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends Account {
    
    @Column(nullable = false)
    private String fullName;
    
    @Column
    private String email; // Optional for notifications
    
    @Column(nullable = false)
    private Boolean isSuperAdmin = false; // Super admin có thể tạo admin khác
}

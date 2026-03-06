package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "moderators")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Moderator extends Account {
    
    @Column(nullable = false)
    private String fullName;
    
    @Column
    private String email; // Optional for notifications
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Admin createdBy; // Admin nào tạo moderator này
    
    @Column(nullable = false)
    private Boolean canBlockMoments = true;
    
    @Column(nullable = false)
    private Boolean canBlockComments = true;
    
    @Column(nullable = false)
    private Boolean canBlockUsers = false; // Chỉ admin mới block users
}

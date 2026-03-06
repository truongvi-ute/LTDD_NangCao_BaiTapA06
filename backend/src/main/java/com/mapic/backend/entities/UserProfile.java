package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    
    @Id
    private Long id; // Same as user_id
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(length = 500)
    private String bio;
    
    @Column
    private String avatarUrl;
    
    @Column
    private String coverImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;
    
    @Column
    private LocalDate dateOfBirth;
    
    @Column
    private String location;
    
    @Column
    private String website;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

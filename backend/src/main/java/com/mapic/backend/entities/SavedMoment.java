package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_moments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "moment_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedMoment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;
    
    @PrePersist
    protected void onCreate() {
        savedAt = LocalDateTime.now();
    }
}

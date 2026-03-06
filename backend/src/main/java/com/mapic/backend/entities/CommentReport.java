package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Column
    private LocalDateTime reviewedAt;
    
    @Column(length = 500)
    private String reviewNote;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

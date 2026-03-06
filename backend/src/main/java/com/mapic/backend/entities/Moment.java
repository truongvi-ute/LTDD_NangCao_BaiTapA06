package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "moments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private String imageUrl;

    @Column(length = 1000)
    private String caption;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String addressName;

    @Column(nullable = false)
    private Boolean isPublic = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MomentCategory category = MomentCategory.OTHER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_id")
    private Province province;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MomentStatus status = MomentStatus.ACTIVE;

    @Column(nullable = false)
    private Long reactionCount = 0L;

    @Column(nullable = false)
    private Long commentCount = 0L;

    @Column(nullable = false)
    private Long saveCount = 0L;

    @Column(nullable = false)
    private Long reportCount = 0L;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.mapic.backend.dtos;

import com.mapic.backend.entities.MomentCategory;
import com.mapic.backend.entities.MomentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomentDto {
    private Long id;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String imageUrl;
    private String caption;
    private Double latitude;
    private Double longitude;
    private String addressName;
    private Boolean isPublic;
    private MomentCategory category;
    private MomentStatus status;
    private Long reactionCount;
    private Long commentCount;
    private Long saveCount;
    private LocalDateTime createdAt;
    
    // Province information
    private String provinceName;
    private String provinceCode;
}

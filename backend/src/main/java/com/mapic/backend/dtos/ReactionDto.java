package com.mapic.backend.dtos;

import com.mapic.backend.entities.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDto {
    private Long id;
    private Long momentId;
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private ReactionType type;
    private LocalDateTime createdAt;
}

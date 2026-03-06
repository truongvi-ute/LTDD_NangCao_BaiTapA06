package com.mapic.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private Long momentId;
    private Long userId;
    private String userName;
    private String userAvatarUrl;
    private String content;
    private Long parentCommentId;
    private List<CommentDto> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

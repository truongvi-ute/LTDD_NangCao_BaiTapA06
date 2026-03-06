package com.mapic.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipDto {
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String avatarUrl;
    private String status; // PENDING, ACCEPTED, BLOCKED
    private String type; // SENT or RECEIVED (for pending requests)
    private String createdAt;
}

package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.dtos.FriendshipDto;
import com.mapic.backend.entities.User;
import com.mapic.backend.services.FriendshipService;
import com.mapic.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {
    
    private final FriendshipService friendshipService;
    
    @PostMapping("/send-request")
    public ResponseEntity<ApiResponse<Void>> sendFriendRequest(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            String username = request.get("username");
            
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Tên tài khoản không được để trống", null));
            }
            
            friendshipService.sendFriendRequest(userId, username.trim());
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã gửi lời mời kết bạn", null));
        } catch (Exception e) {
            log.error("Error sending friend request", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @PathVariable Long friendshipId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            friendshipService.acceptFriendRequest(userId, friendshipId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã chấp nhận lời mời kết bạn", null));
        } catch (Exception e) {
            log.error("Error accepting friend request", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @PostMapping("/reject/{friendshipId}")
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @PathVariable Long friendshipId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            friendshipService.rejectFriendRequest(userId, friendshipId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã từ chối lời mời kết bạn", null));
        } catch (Exception e) {
            log.error("Error rejecting friend request", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/unfriend/{friendshipId}")
    public ResponseEntity<ApiResponse<Void>> unfriend(
            @PathVariable Long friendshipId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            friendshipService.unfriend(userId, friendshipId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Đã hủy kết bạn", null));
        } catch (Exception e) {
            log.error("Error unfriending", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<FriendshipDto>>> getFriends(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            List<FriendshipDto> friends = friendshipService.getFriends(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách bạn bè thành công", friends));
        } catch (Exception e) {
            log.error("Error getting friends list", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<List<FriendshipDto>>> getPendingRequests(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            List<FriendshipDto> requests = friendshipService.getPendingRequests(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách lời mời thành công", requests));
        } catch (Exception e) {
            log.error("Error getting pending requests", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> searchUser(
            @RequestParam String name,
            Authentication authentication) {
        try {
            List<User> users = friendshipService.searchUserByName(name);
            
            List<Map<String, Object>> data = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("name", user.getName());
                userMap.put("avatarUrl", UserService.buildAvatarUrl(user.getProfile() != null ? user.getProfile().getAvatarUrl() : null));
                data.add(userMap);
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Tìm thấy người dùng", data));
        } catch (Exception e) {
            log.error("Error searching user", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}

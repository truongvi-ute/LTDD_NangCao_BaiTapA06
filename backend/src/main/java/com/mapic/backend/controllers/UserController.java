package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.entities.User;
import com.mapic.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/upload-avatar")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            String filename = userService.uploadAvatar(userId, file);
            
            Map<String, String> data = new HashMap<>();
            data.put("avatarUrl", UserService.buildAvatarUrl(filename));
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Upload avatar thành công", data));
        } catch (Exception e) {
            log.error("Error uploading avatar", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Không thể upload avatar: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            
            String name = (String) request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Tên không được để trống", null));
            }
            
            String bio = (String) request.get("bio");
            String gender = (String) request.get("gender");
            String dateOfBirth = (String) request.get("dateOfBirth");
            String location = (String) request.get("location");
            String website = (String) request.get("website");
            
            userService.updateProfile(userId, name.trim(), bio, gender, dateOfBirth, location, website);
            
            Map<String, Object> data = new HashMap<>();
            data.put("name", name.trim());
            data.put("bio", bio);
            data.put("gender", gender);
            data.put("dateOfBirth", dateOfBirth);
            data.put("location", location);
            data.put("website", website);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật thông tin thành công", data));
        } catch (Exception e) {
            log.error("Error updating profile", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Không thể cập nhật thông tin: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/update-name")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateName(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            String name = request.get("name");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Tên không được để trống", null));
            }
            
            userService.updateName(userId, name.trim());
            
            Map<String, String> data = new HashMap<>();
            data.put("name", name.trim());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật tên thành công", data));
        } catch (Exception e) {
            log.error("Error updating name", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Không thể cập nhật tên: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProfile(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            User user = userService.getUserById(userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("name", user.getName());
            data.put("email", user.getEmail());
            
            if (user.getProfile() != null) {
                data.put("avatarUrl", UserService.buildAvatarUrl(user.getProfile().getAvatarUrl()));
                data.put("bio", user.getProfile().getBio());
                data.put("gender", user.getProfile().getGender() != null ? user.getProfile().getGender().toString() : null);
                data.put("dateOfBirth", user.getProfile().getDateOfBirth() != null ? user.getProfile().getDateOfBirth().toString() : null);
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin thành công", data));
        } catch (Exception e) {
            log.error("Error getting profile", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Không thể lấy thông tin: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProfile(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            User user = userService.getUserById(userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("name", user.getName());
            data.put("email", user.getEmail());
            
            if (user.getProfile() != null) {
                data.put("avatarUrl", UserService.buildAvatarUrl(user.getProfile().getAvatarUrl()));
                data.put("bio", user.getProfile().getBio());
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin thành công", data));
        } catch (Exception e) {
            log.error("Error getting user profile", e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Không thể lấy thông tin: " + e.getMessage(), null));
        }
    }
}

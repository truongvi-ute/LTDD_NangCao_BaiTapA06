package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.dtos.CreateMomentRequest;
import com.mapic.backend.dtos.MomentDto;
import com.mapic.backend.services.MomentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/moments")
@RequiredArgsConstructor
public class MomentController {
    
    private final MomentService momentService;
    
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MomentDto>> createMoment(
            @RequestParam("image") MultipartFile image,
            @RequestParam("caption") String caption,
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam("addressName") String addressName,
            @RequestParam("isPublic") Boolean isPublic,
            @RequestParam("category") String category,
            Authentication authentication) {
        
        try {
            Long userId = Long.parseLong(authentication.getName());
            
            // Validate image
            if (image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Vui lòng chọn ảnh", null));
            }
            
            // Validate file type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "File phải là ảnh", null));
            }
            
            // Validate file size (max 10MB)
            if (image.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "Kích thước ảnh không được vượt quá 10MB", null));
            }
            
            // Create request
            CreateMomentRequest request = new CreateMomentRequest();
            request.setCaption(caption);
            request.setLatitude(latitude);
            request.setLongitude(longitude);
            request.setAddressName(addressName);
            request.setIsPublic(isPublic);
            request.setCategory(com.mapic.backend.entities.MomentCategory.valueOf(category));
            
            MomentDto moment = momentService.createMoment(userId, request, image);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Tạo moment thành công", moment));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Danh mục không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/my-moments")
    public ResponseEntity<ApiResponse<List<MomentDto>>> getMyMoments(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            List<MomentDto> moments = momentService.getUserMoments(userId);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách moment thành công", moments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<List<MomentDto>>> getFeed(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            System.out.println("Getting feed for user " + userId);
            List<MomentDto> moments = momentService.getFeedMoments(userId);
            System.out.println("Found " + moments.size() + " moments in feed");
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy feed thành công", moments));
        } catch (Exception e) {
            System.err.println("Error getting feed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/province/{provinceName}")
    public ResponseEntity<ApiResponse<List<MomentDto>>> getMomentsByProvince(
            @PathVariable String provinceName,
            Authentication authentication) {
        try {
            System.out.println("Getting moments for province: " + provinceName);
            List<MomentDto> moments = momentService.getMomentsByProvince(provinceName);
            System.out.println("Found " + moments.size() + " moments");
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy moments theo tỉnh thành công", moments));
        } catch (Exception e) {
            System.err.println("Error getting moments by province: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<MomentDto>>> getUserMomentsByUserId(
            @PathVariable Long userId,
            Authentication authentication) {
        try {
            Long currentUserId = Long.parseLong(authentication.getName());
            System.out.println("Getting moments for user " + userId + " by viewer " + currentUserId);
            List<MomentDto> moments = momentService.getUserMomentsForViewer(userId, currentUserId);
            System.out.println("Found " + moments.size() + " moments");

            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách moment thành công", moments));
        } catch (RuntimeException e) {
            System.err.println("Error getting user moments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("Error getting user moments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/{momentId}")
    public ResponseEntity<ApiResponse<MomentDto>> getMoment(
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            MomentDto moment = momentService.getMomentById(momentId, userId);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy moment thành công", moment));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }    @DeleteMapping("/{momentId}")
    public ResponseEntity<ApiResponse<Void>> deleteMoment(
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            momentService.deleteMoment(momentId, userId);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Xóa moment thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }

}

package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.dtos.CreateMomentRequest;
import com.mapic.backend.dtos.MomentDto;
import com.mapic.backend.dtos.PageResponse;
import com.mapic.backend.entities.Moment;
import com.mapic.backend.entities.SavedMoment;
import com.mapic.backend.entities.User;
import com.mapic.backend.repositories.MomentRepository;
import com.mapic.backend.repositories.SavedMomentRepository;
import com.mapic.backend.repositories.UserRepository;
import com.mapic.backend.services.MomentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/moments")
@RequiredArgsConstructor
public class MomentController {
    
    private final MomentService momentService;
    private final SavedMomentRepository savedMomentRepository;
    private final MomentRepository momentRepository;
    private final UserRepository userRepository;
    
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
    
    @GetMapping("/my-moments/paginated")
    public ResponseEntity<ApiResponse<PageResponse<MomentDto>>> getMyMomentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            PageResponse<MomentDto> moments = momentService.getUserMomentsPaginated(userId, page, size);
            
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
    
    @GetMapping("/feed/paginated")
    public ResponseEntity<ApiResponse<PageResponse<MomentDto>>> getFeedPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            System.out.println("Getting paginated feed for user " + userId + " - page: " + page + ", size: " + size);
            PageResponse<MomentDto> moments = momentService.getFeedMomentsPaginated(userId, page, size);
            System.out.println("Found " + moments.getContent().size() + " moments in page " + page);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy feed thành công", moments));
        } catch (Exception e) {
            System.err.println("Error getting paginated feed: " + e.getMessage());
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
    
    @GetMapping("/province/{provinceName}/paginated")
    public ResponseEntity<ApiResponse<PageResponse<MomentDto>>> getMomentsByProvincePaginated(
            @PathVariable String provinceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            System.out.println("Getting paginated moments for province: " + provinceName + " - page: " + page);
            PageResponse<MomentDto> moments = momentService.getMomentsByProvincePaginated(provinceName, page, size);
            System.out.println("Found " + moments.getContent().size() + " moments in page " + page);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy moments theo tỉnh thành công", moments));
        } catch (Exception e) {
            System.err.println("Error getting paginated moments by province: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<MomentDto>>> getMomentsByCategory(
            @PathVariable String category,
            Authentication authentication) {
        try {
            System.out.println("Getting moments for category: " + category);
            List<MomentDto> moments = momentService.getMomentsByCategory(category);
            System.out.println("Found " + moments.size() + " moments");
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy moments theo danh mục thành công", moments));
        } catch (Exception e) {
            System.err.println("Error getting moments by category: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/category/{category}/paginated")
    public ResponseEntity<ApiResponse<PageResponse<MomentDto>>> getMomentsByCategoryPaginated(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            System.out.println("Getting paginated moments for category: " + category + " - page: " + page);
            PageResponse<MomentDto> moments = momentService.getMomentsByCategoryPaginated(category, page, size);
            System.out.println("Found " + moments.getContent().size() + " moments in page " + page);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy moments theo danh mục thành công", moments));
        } catch (Exception e) {
            System.err.println("Error getting paginated moments by category: " + e.getMessage());
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
    
    @GetMapping("/user/{userId}/paginated")
    public ResponseEntity<ApiResponse<PageResponse<MomentDto>>> getUserMomentsByUserIdPaginated(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            Long currentUserId = Long.parseLong(authentication.getName());
            System.out.println("Getting paginated moments for user " + userId + " by viewer " + currentUserId + " - page: " + page);
            PageResponse<MomentDto> moments = momentService.getUserMomentsForViewerPaginated(userId, currentUserId, page, size);
            System.out.println("Found " + moments.getContent().size() + " moments in page " + page);

            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách moment thành công", moments));
        } catch (RuntimeException e) {
            System.err.println("Error getting paginated user moments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("Error getting paginated user moments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<MomentDto>>> getSavedMoments(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            List<MomentDto> moments = momentService.getSavedMoments(userId);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách moment đã lưu thành công", moments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/saved/paginated")
    public ResponseEntity<ApiResponse<PageResponse<MomentDto>>> getSavedMomentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            PageResponse<MomentDto> moments = momentService.getSavedMomentsPaginated(userId, page, size);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách moment đã lưu thành công", moments));
        } catch (Exception e) {
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
    @PostMapping("/{momentId}/save")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleSave(
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            
            Moment moment = momentRepository.findById(momentId)
                    .orElseThrow(() -> new RuntimeException("Moment not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Optional<SavedMoment> existing = savedMomentRepository.findByUserAndMoment(user, moment);
            boolean saved;
            
            if (existing.isPresent()) {
                // Unsave
                savedMomentRepository.delete(existing.get());
                saved = false;
            } else {
                // Save
                SavedMoment savedMoment = new SavedMoment();
                savedMoment.setUser(user);
                savedMoment.setMoment(moment);
                savedMomentRepository.save(savedMoment);
                saved = true;
            }
            
            // Update saveCount on moment
            Long newSaveCount = savedMomentRepository.countByMoment(moment);
            moment.setSaveCount(newSaveCount);
            momentRepository.save(moment);
            
            Map<String, Object> data = new HashMap<>();
            data.put("saved", saved);
            data.put("saveCount", newSaveCount);
            
            return ResponseEntity.ok(new ApiResponse<>(true,
                    saved ? "Đã lưu moment" : "Đã bỏ lưu moment", data));
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{momentId}/is-saved")
    public ResponseEntity<ApiResponse<Map<String, Object>>> isSaved(
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            Moment moment = momentRepository.findById(momentId)
                    .orElseThrow(() -> new RuntimeException("Moment not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean saved = savedMomentRepository.existsByUserAndMoment(user, moment);
            
            Map<String, Object> data = new HashMap<>();
            data.put("saved", saved);
            data.put("saveCount", moment.getSaveCount());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "OK", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
}


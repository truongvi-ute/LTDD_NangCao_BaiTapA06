package com.mapic.backend.services;

import com.mapic.backend.dtos.CreateMomentRequest;
import com.mapic.backend.dtos.MomentDto;
import com.mapic.backend.dtos.PageResponse;
import com.mapic.backend.entities.Moment;
import com.mapic.backend.entities.MomentCategory;
import com.mapic.backend.entities.MomentStatus;
import com.mapic.backend.entities.User;
import com.mapic.backend.entities.Friendship;
import com.mapic.backend.entities.FriendshipStatus;
import com.mapic.backend.repositories.MomentRepository;
import com.mapic.backend.repositories.UserRepository;
import com.mapic.backend.repositories.FriendshipRepository;
import com.mapic.backend.repositories.SavedMomentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MomentService {
    
    private final MomentRepository momentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final ProvinceService provinceService;
    private final SavedMomentRepository savedMomentRepository;
    
    private static final String UPLOAD_DIR = "uploads/moments/";
    
    @Transactional
    public MomentDto createMoment(Long userId, CreateMomentRequest request, MultipartFile image) {
        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Upload image
        String imageUrl = uploadImage(image);
        
        // Create moment
        Moment moment = new Moment();
        moment.setAuthor(user);
        moment.setImageUrl(imageUrl);
        moment.setCaption(request.getCaption());
        moment.setLatitude(request.getLatitude());
        moment.setLongitude(request.getLongitude());
        moment.setAddressName(request.getAddressName());
        moment.setIsPublic(request.getIsPublic());
        moment.setCategory(request.getCategory());
        moment.setStatus(MomentStatus.ACTIVE);
        
        // Auto-detect and set province
        detectAndSetProvince(moment);
        
        Moment savedMoment = momentRepository.save(moment);
        
        return convertToDto(savedMoment);
    }
    
    public List<MomentDto> getUserMoments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Moment> moments = momentRepository.findByAuthorAndStatusOrderByCreatedAtDesc(
                user, MomentStatus.ACTIVE);
        
        return moments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PageResponse<MomentDto> getUserMomentsPaginated(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Moment> momentPage = momentRepository.findByAuthorAndStatusOrderByCreatedAtDesc(
                user, MomentStatus.ACTIVE, pageable);
        
        List<MomentDto> momentDtos = momentPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                momentDtos,
                momentPage.getNumber(),
                momentPage.getSize(),
                momentPage.getTotalElements(),
                momentPage.getTotalPages(),
                momentPage.isLast(),
                momentPage.isFirst()
        );
    }
    
    public List<MomentDto> getUserMomentsForViewer(Long targetUserId, Long viewerId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Moment> moments;
        
        // If viewing own profile, show all moments
        if (targetUserId.equals(viewerId)) {
            moments = momentRepository.findByAuthorAndStatusOrderByCreatedAtDesc(
                    targetUser, MomentStatus.ACTIVE);
        } else {
            // Check if they are friends
            boolean isFriend = friendshipRepository.findFriendshipBetween(targetUserId, viewerId)
                    .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                    .orElse(false);
                    
            if (isFriend) {
                // Friends can see all moments
                moments = momentRepository.findByAuthorAndStatusOrderByCreatedAtDesc(
                        targetUser, MomentStatus.ACTIVE);
            } else {
                // Non-friends only see public moments
                moments = momentRepository.findByAuthorAndStatusOrderByCreatedAtDesc(
                        targetUser, MomentStatus.ACTIVE)
                        .stream()
                        .filter(Moment::getIsPublic)
                        .collect(Collectors.toList());
            }
        }
        
        return moments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PageResponse<MomentDto> getUserMomentsForViewerPaginated(Long targetUserId, Long viewerId, int page, int size) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Moment> momentPage = momentRepository.findByAuthorAndStatusOrderByCreatedAtDesc(
                targetUser, MomentStatus.ACTIVE, pageable);
        
        List<MomentDto> momentDtos;
        
        // If viewing own profile, show all moments
        if (targetUserId.equals(viewerId)) {
            momentDtos = momentPage.getContent().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            // Check if they are friends
            boolean isFriend = friendshipRepository.findFriendshipBetween(targetUserId, viewerId)
                    .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                    .orElse(false);
                    
            if (isFriend) {
                // Friends can see all moments
                momentDtos = momentPage.getContent().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
            } else {
                // Non-friends only see public moments
                momentDtos = momentPage.getContent().stream()
                        .filter(Moment::getIsPublic)
                        .map(this::convertToDto)
                        .collect(Collectors.toList());
            }
        }
        
        return new PageResponse<>(
                momentDtos,
                momentPage.getNumber(),
                momentPage.getSize(),
                momentPage.getTotalElements(),
                momentPage.getTotalPages(),
                momentPage.isLast(),
                momentPage.isFirst()
        );
    }
    
    public List<MomentDto> getFeedMoments(Long userId) {
        List<Moment> moments = momentRepository.findMomentsForUserFeed(userId, MomentStatus.ACTIVE);
        
        return moments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PageResponse<MomentDto> getFeedMomentsPaginated(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Moment> momentPage = momentRepository.findMomentsForUserFeed(userId, MomentStatus.ACTIVE, pageable);
        
        List<MomentDto> momentDtos = momentPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                momentDtos,
                momentPage.getNumber(),
                momentPage.getSize(),
                momentPage.getTotalElements(),
                momentPage.getTotalPages(),
                momentPage.isLast(),
                momentPage.isFirst()
        );
    }
    
    public List<MomentDto> getMomentsByProvince(String provinceName) {
        List<Moment> moments = momentRepository.findByProvinceNameContaining(provinceName, MomentStatus.ACTIVE);
        
        // Only return public moments for explore feature
        return moments.stream()
                .filter(Moment::getIsPublic)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PageResponse<MomentDto> getMomentsByProvincePaginated(String provinceName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Moment> momentPage = momentRepository.findByProvinceNameContaining(provinceName, MomentStatus.ACTIVE, pageable);
        
        // Only return public moments for explore feature
        List<MomentDto> momentDtos = momentPage.getContent().stream()
                .filter(Moment::getIsPublic)
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                momentDtos,
                momentPage.getNumber(),
                momentPage.getSize(),
                momentPage.getTotalElements(),
                momentPage.getTotalPages(),
                momentPage.isLast(),
                momentPage.isFirst()
        );
    }
    
    public List<MomentDto> getMomentsByCategory(String categoryStr) {
        try {
            MomentCategory category = MomentCategory.valueOf(categoryStr.toUpperCase());
            List<Moment> moments = momentRepository.findByCategoryAndStatusOrderByCreatedAtDesc(
                    category, MomentStatus.ACTIVE);
            
            // Only return public moments for explore feature
            return moments.stream()
                    .filter(Moment::getIsPublic)
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + categoryStr);
        }
    }
    
    public PageResponse<MomentDto> getMomentsByCategoryPaginated(String categoryStr, int page, int size) {
        try {
            MomentCategory category = MomentCategory.valueOf(categoryStr.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Moment> momentPage = momentRepository.findByCategoryAndStatusOrderByCreatedAtDesc(
                    category, MomentStatus.ACTIVE, pageable);
            
            // Only return public moments for explore feature
            List<MomentDto> momentDtos = momentPage.getContent().stream()
                    .filter(Moment::getIsPublic)
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return new PageResponse<>(
                    momentDtos,
                    momentPage.getNumber(),
                    momentPage.getSize(),
                    momentPage.getTotalElements(),
                    momentPage.getTotalPages(),
                    momentPage.isLast(),
                    momentPage.isFirst()
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid category: " + categoryStr);
        }
    }
    
    public List<MomentDto> getSavedMoments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Moment> moments = savedMomentRepository.findMomentsByUser(user);
        
        // Filter out deleted moments
        return moments.stream()
                .filter(m -> m.getStatus() == MomentStatus.ACTIVE)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public PageResponse<MomentDto> getSavedMomentsPaginated(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Moment> momentPage = savedMomentRepository.findMomentsByUser(user, pageable);
        
        // Filter out deleted moments
        List<MomentDto> momentDtos = momentPage.getContent().stream()
                .filter(m -> m.getStatus() == MomentStatus.ACTIVE)
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                momentDtos,
                momentPage.getNumber(),
                momentPage.getSize(),
                momentPage.getTotalElements(),
                momentPage.getTotalPages(),
                momentPage.isLast(),
                momentPage.isFirst()
        );
    }
    
    public MomentDto getMomentById(Long momentId, Long userId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        // Check permission
        if (!moment.getIsPublic() && !moment.getAuthor().getId().equals(userId)) {
            // Check if they are friends
            boolean isFriend = friendshipRepository.findFriendshipBetween(moment.getAuthor().getId(), userId)
                    .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED)
                    .orElse(false);
                    
            if (!isFriend) {
                throw new RuntimeException("You don't have permission to view this moment");
            }
        }
        
        return convertToDto(moment);
    }
    
    @Transactional
    public void deleteMoment(Long momentId, Long userId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        // Check if user is the author
        if (!moment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this moment");
        }
        
        // Soft delete
        moment.setStatus(MomentStatus.DELETED);
        momentRepository.save(moment);
    }
    
    private String uploadImage(MultipartFile file) {
        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }
    
    private MomentDto convertToDto(Moment moment) {
        MomentDto dto = new MomentDto();
        dto.setId(moment.getId());
        dto.setAuthorId(moment.getAuthor().getId());
        dto.setAuthorName(moment.getAuthor().getName());
        
        // Get author avatar
        if (moment.getAuthor().getProfile() != null) {
            dto.setAuthorAvatarUrl(moment.getAuthor().getProfile().getAvatarUrl());
        }
        
        dto.setImageUrl(moment.getImageUrl());
        dto.setCaption(moment.getCaption());
        dto.setLatitude(moment.getLatitude());
        dto.setLongitude(moment.getLongitude());
        dto.setAddressName(moment.getAddressName());
        dto.setIsPublic(moment.getIsPublic());
        dto.setCategory(moment.getCategory());
        dto.setStatus(moment.getStatus());
        dto.setReactionCount(moment.getReactionCount());
        dto.setCommentCount(moment.getCommentCount());
        dto.setSaveCount(moment.getSaveCount());
        dto.setCreatedAt(moment.getCreatedAt());
        
        // Add province information
        if (moment.getProvince() != null) {
            dto.setProvinceName(moment.getProvince().getName());
            dto.setProvinceCode(moment.getProvince().getCode());
        }
        
        return dto;
    }
    
    /**
     * Auto-detect province from coordinates and address
     */
    private void detectAndSetProvince(Moment moment) {
        // First try to detect from address name
        provinceService.detectProvinceFromAddress(moment.getAddressName())
                .ifPresentOrElse(
                        moment::setProvince,
                        () -> {
                            // If not found in address, try by coordinates
                            provinceService.findClosestProvince(moment.getLatitude(), moment.getLongitude())
                                    .ifPresent(moment::setProvince);
                        }
                );
    }
}

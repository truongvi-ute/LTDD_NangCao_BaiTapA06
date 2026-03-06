package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.dtos.ReactionDto;
import com.mapic.backend.entities.ReactionType;
import com.mapic.backend.services.ReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {
    
    private final ReactionService reactionService;
    
    @PostMapping("/moment/{momentId}")
    public ResponseEntity<ApiResponse<ReactionDto>> toggleReaction(
            @PathVariable Long momentId,
            @RequestParam String type,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            ReactionType reactionType = ReactionType.valueOf(type.toUpperCase());
            
            ReactionDto reaction = reactionService.toggleReaction(momentId, userId, reactionType);
            
            String message = reaction == null ? "Đã bỏ thích" : "Đã thả cảm xúc";
            return ResponseEntity.ok(new ApiResponse<>(true, message, reaction));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Loại cảm xúc không hợp lệ", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/moment/{momentId}")
    public ResponseEntity<ApiResponse<List<ReactionDto>>> getMomentReactions(
            @PathVariable Long momentId) {
        try {
            List<ReactionDto> reactions = reactionService.getMomentReactions(momentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách cảm xúc thành công", reactions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/moment/{momentId}/my-reaction")
    public ResponseEntity<ApiResponse<ReactionDto>> getMyReaction(
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            ReactionDto reaction = reactionService.getUserReaction(momentId, userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy cảm xúc thành công", reaction));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
}

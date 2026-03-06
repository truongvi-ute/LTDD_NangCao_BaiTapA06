package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.dtos.CommentDto;
import com.mapic.backend.dtos.CreateCommentRequest;
import com.mapic.backend.services.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @PostMapping("/moment/{momentId}")
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable Long momentId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            CommentDto comment = commentService.createComment(momentId, userId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Bình luận thành công", comment));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/moment/{momentId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getMomentComments(
            @PathVariable Long momentId) {
        try {
            List<CommentDto> comments = commentService.getMomentComments(momentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách bình luận thành công", comments));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            commentService.deleteComment(commentId, userId);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Xóa bình luận thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }
}

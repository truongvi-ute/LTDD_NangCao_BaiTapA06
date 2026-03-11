package com.mapic.backend.controllers;

import com.mapic.backend.dtos.AlbumDto;
import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.dtos.CreateAlbumRequest;
import com.mapic.backend.services.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlbumDto>>> getMyAlbums(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            List<AlbumDto> albums = albumService.getUserAlbums(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách album thành công", albums));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi: " + e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlbumDto>> createAlbum(
            @Valid @RequestBody CreateAlbumRequest request,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            AlbumDto albumDto = albumService.createAlbum(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Tạo album thành công", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi tạo album", null));
        }
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumDto>> getAlbumDetails(
            @PathVariable Long albumId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            AlbumDto albumDto = albumService.getAlbumDetails(albumId, userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chi tiết album", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi lấy chi tiết album", null));
        }
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<ApiResponse<AlbumDto>> updateAlbum(
            @PathVariable Long albumId,
            @Valid @RequestBody CreateAlbumRequest request,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            AlbumDto albumDto = albumService.updateAlbum(albumId, userId, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Cập nhật album thành công", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi cập nhật album", null));
        }
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlbum(
            @PathVariable Long albumId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            albumService.deleteAlbum(albumId, userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Xóa album thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi xóa album", null));
        }
    }

    @PostMapping("/{albumId}/moments/{momentId}")
    public ResponseEntity<ApiResponse<AlbumDto>> addMomentToAlbum(
            @PathVariable Long albumId,
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            AlbumDto albumDto = albumService.addMomentToAlbum(albumId, momentId, userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Thêm moment vào album thành công", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi khi thêm moment", null));
        }
    }

    @DeleteMapping("/{albumId}/moments/{momentId}")
    public ResponseEntity<ApiResponse<AlbumDto>> removeMomentFromAlbum(
            @PathVariable Long albumId,
            @PathVariable Long momentId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            AlbumDto albumDto = albumService.removeMomentFromAlbum(albumId, momentId, userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Bỏ lưu moment từ album thành công", albumDto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Lỗi bỏ lưu moment khỏi album", null));
        }
    }
}

package com.mapic.backend.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAlbumRequest {
    
    @NotBlank(message = "Tên album không được để trống")
    @Size(max = 255, message = "Tên album không được quá 255 ký tự")
    private String name;
    
    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;
}

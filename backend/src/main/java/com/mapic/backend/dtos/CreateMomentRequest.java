package com.mapic.backend.dtos;

import com.mapic.backend.entities.MomentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomentRequest {
    
    @NotBlank(message = "Caption không được để trống")
    private String caption;
    
    @NotNull(message = "Latitude không được để trống")
    private Double latitude;
    
    @NotNull(message = "Longitude không được để trống")
    private Double longitude;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String addressName;
    
    @NotNull(message = "Trạng thái công khai không được để trống")
    private Boolean isPublic;
    
    @NotNull(message = "Danh mục không được để trống")
    private MomentCategory category;
}

package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.entities.Province;
import com.mapic.backend.services.ProvinceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/provinces")
@RequiredArgsConstructor
public class ProvinceController {
    
    private final ProvinceService provinceService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Province>>> getAllProvinces() {
        List<Province> provinces = provinceService.getAllProvinces();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách tỉnh thành công", provinces));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Province>>> searchProvinces(
            @RequestParam String keyword) {
        List<Province> provinces = provinceService.searchProvinces(keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tìm kiếm thành công", provinces));
    }
    
    @GetMapping("/region/{region}")
    public ResponseEntity<ApiResponse<List<Province>>> getProvincesByRegion(
            @PathVariable String region) {
        List<Province> provinces = provinceService.getProvincesByRegion(region);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lấy danh sách tỉnh theo vùng thành công", provinces));
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<Province>> getProvinceByCode(
            @PathVariable String code) {
        return provinceService.getProvinceByCode(code)
                .map(province -> ResponseEntity.ok(new ApiResponse<>(true, "Lấy thông tin tỉnh thành công", province)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/closest")
    public ResponseEntity<ApiResponse<Province>> getClosestProvince(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        return provinceService.findClosestProvince(latitude, longitude)
                .map(province -> ResponseEntity.ok(new ApiResponse<>(true, "Tìm tỉnh gần nhất thành công", province)))
                .orElse(ResponseEntity.ok(new ApiResponse<>(false, "Không tìm thấy tỉnh", null)));
    }
}

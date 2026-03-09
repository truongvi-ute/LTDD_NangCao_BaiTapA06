package com.mapic.backend.services;

import com.mapic.backend.entities.Province;
import com.mapic.backend.repositories.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProvinceService {
    
    private final ProvinceRepository provinceRepository;
    
    public List<Province> getAllProvinces() {
        return provinceRepository.findAll();
    }
    
    public Optional<Province> getProvinceByCode(String code) {
        return provinceRepository.findByCode(code);
    }
    
    public Optional<Province> getProvinceByName(String name) {
        return provinceRepository.findByName(name);
    }
    
    public List<Province> getProvincesByRegion(String region) {
        return provinceRepository.findByRegion(region);
    }
    
    public List<Province> searchProvinces(String keyword) {
        return provinceRepository.searchByKeyword(keyword);
    }
    
    /**
     * Find the closest province based on coordinates
     * Uses Haversine formula to calculate distance
     */
    public Optional<Province> findClosestProvince(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return Optional.empty();
        }
        return provinceRepository.findClosestProvince(latitude, longitude);
    }
    
    /**
     * Detect province from address name
     * Tries to match province name in the address string
     */
    public Optional<Province> detectProvinceFromAddress(String addressName) {
        if (addressName == null || addressName.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // Try to find province by searching in address
        List<Province> allProvinces = provinceRepository.findAll();
        
        for (Province province : allProvinces) {
            if (addressName.toLowerCase().contains(province.getName().toLowerCase()) ||
                addressName.toLowerCase().contains(province.getNameEn().toLowerCase())) {
                return Optional.of(province);
            }
        }
        
        return Optional.empty();
    }
}

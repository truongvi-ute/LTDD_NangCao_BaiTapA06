package com.mapic.backend.repositories;

import com.mapic.backend.entities.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
    
    Optional<Province> findByCode(String code);
    
    Optional<Province> findByName(String name);
    
    List<Province> findByRegion(String region);
    
    @Query("SELECT p FROM Province p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.nameEn) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Province> searchByKeyword(@Param("keyword") String keyword);
    
    // Find province by coordinates (closest province)
    @Query(value = "SELECT * FROM provinces p " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
           "cos(radians(p.longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(p.latitude)))) ASC LIMIT 1", 
           nativeQuery = true)
    Optional<Province> findClosestProvince(@Param("lat") Double latitude, @Param("lng") Double longitude);
}

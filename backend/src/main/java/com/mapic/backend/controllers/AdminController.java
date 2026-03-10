package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.entities.Moment;
import com.mapic.backend.repositories.MomentRepository;
import com.mapic.backend.services.DataSeederService;
import com.mapic.backend.services.MomentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final DataSeederService dataSeederService;
    private final MomentStatsService momentStatsService;
    private final MomentRepository momentRepository;
    
    /**
     * Manual trigger for database seeding
     * WARNING: This should be protected in production
     */
    @PostMapping("/seed-database")
    public ResponseEntity<ApiResponse<String>> seedDatabase() {
        try {
            dataSeederService.run();
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Database seeded successfully", "Seeding completed")
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                new ApiResponse<>(false, "Seeding failed: " + e.getMessage(), null)
            );
        }
    }
    
    /**
     * Recalculate all moment statistics from actual database data
     * Use this to fix inconsistent counts
     */
    @PostMapping("/recalculate-stats")
    public ResponseEntity<ApiResponse<String>> recalculateStats() {
        try {
            momentStatsService.updateAllMomentStats();
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Stats recalculated successfully", "All moment stats updated")
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                new ApiResponse<>(false, "Recalculation failed: " + e.getMessage(), null)
            );
        }
    }
    
    /**
     * Clean invalid moments (those with non-UUID image filenames from failed seeding)
     */
    @DeleteMapping("/clean-invalid-moments")
    public ResponseEntity<ApiResponse<String>> cleanInvalidMoments() {
        try {
            List<Moment> allMoments = momentRepository.findAll();
            int deletedCount = 0;
            
            for (Moment moment : allMoments) {
                String imageUrl = moment.getImageUrl();
                // Check if imageUrl is NOT a UUID format
                if (imageUrl != null && !imageUrl.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|jpeg|png)$")) {
                    momentRepository.delete(moment);
                    deletedCount++;
                }
            }
            
            return ResponseEntity.ok(
                new ApiResponse<>(true, "Cleaned " + deletedCount + " invalid moments", String.valueOf(deletedCount))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                new ApiResponse<>(false, "Cleaning failed: " + e.getMessage(), null)
            );
        }
    }
}

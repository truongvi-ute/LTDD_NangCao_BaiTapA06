package com.mapic.backend.controllers;

import com.mapic.backend.dtos.ApiResponse;
import com.mapic.backend.services.DataSeederService;
import com.mapic.backend.services.MomentStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final DataSeederService dataSeederService;
    private final MomentStatsService momentStatsService;
    
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
}

package com.mapic.backend.services;

import com.mapic.backend.entities.Moment;
import com.mapic.backend.repositories.CommentRepository;
import com.mapic.backend.repositories.MomentRepository;
import com.mapic.backend.repositories.ReactionRepository;
import com.mapic.backend.repositories.SavedMomentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service to calculate and update moment statistics
 * This ensures counts are accurate based on actual database data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MomentStatsService {
    
    private final MomentRepository momentRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final SavedMomentRepository savedMomentRepository;
    
    /**
     * Recalculate and update all stats for a specific moment
     */
    @Transactional
    public void updateMomentStats(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        updateMomentStats(moment);
        momentRepository.save(moment);
        
        log.debug("Updated stats for moment {}: reactions={}, comments={}, saves={}", 
                momentId, moment.getReactionCount(), moment.getCommentCount(), moment.getSaveCount());
    }
    
    /**
     * Recalculate and update all stats for a moment entity
     */
    @Transactional
    public void updateMomentStats(Moment moment) {
        // Count reactions
        Long reactionCount = reactionRepository.countByMoment(moment);
        moment.setReactionCount(reactionCount);
        
        // Count comments (only non-blocked comments)
        Long commentCount = commentRepository.countByMomentAndIsBlockedFalse(moment);
        moment.setCommentCount(commentCount);
        
        // Count saves
        Long saveCount = savedMomentRepository.countByMoment(moment);
        moment.setSaveCount(saveCount);
    }
    
    /**
     * Recalculate and update stats for all moments
     * Use this for batch updates or data migration
     */
    @Transactional
    public void updateAllMomentStats() {
        log.info("Starting to update stats for all moments...");
        
        List<Moment> moments = momentRepository.findAll();
        int count = 0;
        
        for (Moment moment : moments) {
            updateMomentStats(moment);
            count++;
            
            // Save in batches of 50
            if (count % 50 == 0) {
                momentRepository.flush();
                log.info("Updated stats for {} moments", count);
            }
        }
        
        momentRepository.flush();
        log.info("Completed updating stats for {} moments", count);
    }
    
    /**
     * Increment reaction count for a moment
     */
    @Transactional
    public void incrementReactionCount(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        moment.setReactionCount(moment.getReactionCount() + 1);
        momentRepository.save(moment);
    }
    
    /**
     * Decrement reaction count for a moment
     */
    @Transactional
    public void decrementReactionCount(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        moment.setReactionCount(Math.max(0, moment.getReactionCount() - 1));
        momentRepository.save(moment);
    }
    
    /**
     * Increment comment count for a moment
     */
    @Transactional
    public void incrementCommentCount(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        moment.setCommentCount(moment.getCommentCount() + 1);
        momentRepository.save(moment);
    }
    
    /**
     * Decrement comment count for a moment
     */
    @Transactional
    public void decrementCommentCount(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        moment.setCommentCount(Math.max(0, moment.getCommentCount() - 1));
        momentRepository.save(moment);
    }
    
    /**
     * Increment save count for a moment
     */
    @Transactional
    public void incrementSaveCount(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        moment.setSaveCount(moment.getSaveCount() + 1);
        momentRepository.save(moment);
    }
    
    /**
     * Decrement save count for a moment
     */
    @Transactional
    public void decrementSaveCount(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        moment.setSaveCount(Math.max(0, moment.getSaveCount() - 1));
        momentRepository.save(moment);
    }
}

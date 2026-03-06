package com.mapic.backend.services;

import com.mapic.backend.dtos.ReactionDto;
import com.mapic.backend.entities.Moment;
import com.mapic.backend.entities.Reaction;
import com.mapic.backend.entities.ReactionType;
import com.mapic.backend.entities.User;
import com.mapic.backend.repositories.MomentRepository;
import com.mapic.backend.repositories.ReactionRepository;
import com.mapic.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReactionService {
    
    private final ReactionRepository reactionRepository;
    private final MomentRepository momentRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ReactionDto toggleReaction(Long momentId, Long userId, ReactionType type) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<Reaction> existingReaction = reactionRepository.findByMomentAndUser(moment, user);
        
        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            
            // If same type, remove reaction (unlike)
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
                updateMomentReactionCount(moment);
                return null;
            } else {
                // Change reaction type
                reaction.setType(type);
                Reaction saved = reactionRepository.save(reaction);
                return convertToDto(saved);
            }
        } else {
            // Create new reaction
            Reaction reaction = new Reaction();
            reaction.setMoment(moment);
            reaction.setUser(user);
            reaction.setType(type);
            
            Reaction saved = reactionRepository.save(reaction);
            updateMomentReactionCount(moment);
            
            return convertToDto(saved);
        }
    }
    
    public List<ReactionDto> getMomentReactions(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        List<Reaction> reactions = reactionRepository.findByMoment(moment);
        
        return reactions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public ReactionDto getUserReaction(Long momentId, Long userId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<Reaction> reaction = reactionRepository.findByMomentAndUser(moment, user);
        
        return reaction.map(this::convertToDto).orElse(null);
    }
    
    private void updateMomentReactionCount(Moment moment) {
        Long count = reactionRepository.countByMoment(moment);
        moment.setReactionCount(count);
        momentRepository.save(moment);
    }
    
    private ReactionDto convertToDto(Reaction reaction) {
        ReactionDto dto = new ReactionDto();
        dto.setId(reaction.getId());
        dto.setMomentId(reaction.getMoment().getId());
        dto.setUserId(reaction.getUser().getId());
        dto.setUserName(reaction.getUser().getName());
        
        if (reaction.getUser().getProfile() != null) {
            dto.setUserAvatarUrl(reaction.getUser().getProfile().getAvatarUrl());
        }
        
        dto.setType(reaction.getType());
        dto.setCreatedAt(reaction.getCreatedAt());
        
        return dto;
    }
}

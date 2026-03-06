package com.mapic.backend.services;

import com.mapic.backend.dtos.CommentDto;
import com.mapic.backend.dtos.CreateCommentRequest;
import com.mapic.backend.entities.Comment;
import com.mapic.backend.entities.Moment;
import com.mapic.backend.entities.User;
import com.mapic.backend.repositories.CommentRepository;
import com.mapic.backend.repositories.MomentRepository;
import com.mapic.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final MomentRepository momentRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public CommentDto createComment(Long momentId, Long userId, CreateCommentRequest request) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment comment = new Comment();
        comment.setMoment(moment);
        comment.setUser(user);
        comment.setContent(request.getContent());
        
        // Handle parent comment (reply)
        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }
        
        Comment saved = commentRepository.save(comment);
        updateMomentCommentCount(moment);
        
        return convertToDto(saved);
    }
    
    public List<CommentDto> getMomentComments(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("Moment not found"));
        
        // Get only top-level comments (no parent)
        List<Comment> comments = commentRepository
                .findByMomentAndParentCommentIsNullAndIsBlockedFalseOrderByCreatedAtAsc(moment);
        
        return comments.stream()
                .map(this::convertToDtoWithReplies)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        // Check if user is the author
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }
        
        Moment moment = comment.getMoment();
        commentRepository.delete(comment);
        updateMomentCommentCount(moment);
    }
    
    private void updateMomentCommentCount(Moment moment) {
        Long count = commentRepository.countByMomentAndIsBlockedFalse(moment);
        moment.setCommentCount(count);
        momentRepository.save(moment);
    }
    
    private CommentDto convertToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setMomentId(comment.getMoment().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getName());
        
        if (comment.getUser().getProfile() != null) {
            dto.setUserAvatarUrl(comment.getUser().getProfile().getAvatarUrl());
        }
        
        dto.setContent(comment.getContent());
        
        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }
        
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setReplies(new ArrayList<>());
        
        return dto;
    }
    
    private CommentDto convertToDtoWithReplies(Comment comment) {
        CommentDto dto = convertToDto(comment);
        
        // Get replies (comments with this comment as parent)
        List<Comment> replies = commentRepository.findByMomentAndIsBlockedFalseOrderByCreatedAtAsc(comment.getMoment())
                .stream()
                .filter(c -> c.getParentComment() != null && c.getParentComment().getId().equals(comment.getId()))
                .collect(Collectors.toList());
        
        dto.setReplies(replies.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
        
        return dto;
    }
}

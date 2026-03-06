package com.mapic.backend.repositories;

import com.mapic.backend.entities.Comment;
import com.mapic.backend.entities.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByMomentAndIsBlockedFalseOrderByCreatedAtAsc(Moment moment);
    
    List<Comment> findByMomentAndParentCommentIsNullAndIsBlockedFalseOrderByCreatedAtAsc(Moment moment);
    
    Long countByMomentAndIsBlockedFalse(Moment moment);
}

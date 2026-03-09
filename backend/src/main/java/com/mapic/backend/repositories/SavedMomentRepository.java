package com.mapic.backend.repositories;

import com.mapic.backend.entities.Moment;
import com.mapic.backend.entities.SavedMoment;
import com.mapic.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedMomentRepository extends JpaRepository<SavedMoment, Long> {
    
    Optional<SavedMoment> findByUserAndMoment(User user, Moment moment);
    
    Long countByMoment(Moment moment);
    
    boolean existsByUserAndMoment(User user, Moment moment);
    
    @Query("SELECT sm.moment FROM SavedMoment sm WHERE sm.user = :user ORDER BY sm.savedAt DESC")
    List<Moment> findMomentsByUser(@Param("user") User user);
    
    @Query("SELECT sm.moment FROM SavedMoment sm WHERE sm.user = :user ORDER BY sm.savedAt DESC")
    Page<Moment> findMomentsByUser(@Param("user") User user, Pageable pageable);
}

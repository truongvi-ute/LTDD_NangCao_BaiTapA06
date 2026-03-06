package com.mapic.backend.repositories;

import com.mapic.backend.entities.Moment;
import com.mapic.backend.entities.Reaction;
import com.mapic.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    Optional<Reaction> findByMomentAndUser(Moment moment, User user);
    
    List<Reaction> findByMoment(Moment moment);
    
    Long countByMoment(Moment moment);
    
    void deleteByMomentAndUser(Moment moment, User user);
}

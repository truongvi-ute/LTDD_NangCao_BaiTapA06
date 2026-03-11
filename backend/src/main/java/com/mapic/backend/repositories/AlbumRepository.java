package com.mapic.backend.repositories;

import com.mapic.backend.entities.Album;
import com.mapic.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    
    List<Album> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Album> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Optional<Album> findByIdAndUser(Long id, User user);
    
    boolean existsByNameAndUser(String name, User user);
}

package com.mapic.backend.repositories;

import com.mapic.backend.entities.Moment;
import com.mapic.backend.entities.MomentCategory;
import com.mapic.backend.entities.MomentStatus;
import com.mapic.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {
    
    // Find all moments by author
    List<Moment> findByAuthorAndStatusOrderByCreatedAtDesc(User author, MomentStatus status);
    
    // Find all moments by author with pagination
    Page<Moment> findByAuthorAndStatusOrderByCreatedAtDesc(User author, MomentStatus status, Pageable pageable);
    
    // Find all public moments
    List<Moment> findByIsPublicTrueAndStatusOrderByCreatedAtDesc(MomentStatus status);
    
    // Find all public moments with pagination
    Page<Moment> findByIsPublicTrueAndStatusOrderByCreatedAtDesc(MomentStatus status, Pageable pageable);
    
    // Find moments by author or friends (for feed)
    @Query("SELECT m FROM Moment m WHERE m.status = :status AND " +
           "(m.isPublic = true OR m.author.id = :userId OR " +
           "m.author.id IN (SELECT f.addressee.id FROM Friendship f WHERE f.requester.id = :userId AND f.status = 'ACCEPTED') OR " +
           "m.author.id IN (SELECT f.requester.id FROM Friendship f WHERE f.addressee.id = :userId AND f.status = 'ACCEPTED')) " +
           "ORDER BY m.createdAt DESC")
    List<Moment> findMomentsForUserFeed(@Param("userId") Long userId, @Param("status") MomentStatus status);
    
    // Find moments by author or friends (for feed) with pagination
    @Query("SELECT m FROM Moment m WHERE m.status = :status AND " +
           "(m.isPublic = true OR m.author.id = :userId OR " +
           "m.author.id IN (SELECT f.addressee.id FROM Friendship f WHERE f.requester.id = :userId AND f.status = 'ACCEPTED') OR " +
           "m.author.id IN (SELECT f.requester.id FROM Friendship f WHERE f.addressee.id = :userId AND f.status = 'ACCEPTED')) " +
           "ORDER BY m.createdAt DESC")
    Page<Moment> findMomentsForUserFeed(@Param("userId") Long userId, @Param("status") MomentStatus status, Pageable pageable);
    
    // Count moments by author
    Long countByAuthorAndStatus(User author, MomentStatus status);
    
    // Find moments by province (using relationship)
    @Query("SELECT m FROM Moment m WHERE m.status = :status AND m.province.name = :provinceName " +
           "ORDER BY m.createdAt DESC")
    List<Moment> findByProvinceName(@Param("provinceName") String provinceName, @Param("status") MomentStatus status);
    
    // Find moments by province name (search in addressName as fallback)
    @Query("SELECT m FROM Moment m WHERE m.status = :status AND " +
           "(m.province.name = :provinceName OR " +
           "LOWER(m.addressName) LIKE LOWER(CONCAT('%', :provinceName, '%'))) " +
           "ORDER BY m.createdAt DESC")
    List<Moment> findByProvinceNameContaining(@Param("provinceName") String provinceName, @Param("status") MomentStatus status);
    
    // Find moments by province name with pagination
    @Query("SELECT m FROM Moment m WHERE m.status = :status AND " +
           "(m.province.name = :provinceName OR " +
           "LOWER(m.addressName) LIKE LOWER(CONCAT('%', :provinceName, '%'))) " +
           "ORDER BY m.createdAt DESC")
    Page<Moment> findByProvinceNameContaining(@Param("provinceName") String provinceName, @Param("status") MomentStatus status, Pageable pageable);
    
    // Find moments by category
    List<Moment> findByCategoryAndStatusOrderByCreatedAtDesc(MomentCategory category, MomentStatus status);
    
    // Find moments by category with pagination
    Page<Moment> findByCategoryAndStatusOrderByCreatedAtDesc(MomentCategory category, MomentStatus status, Pageable pageable);
}

package com.mapic.backend.repositories;

import com.mapic.backend.entities.Friendship;
import com.mapic.backend.entities.FriendshipStatus;
import com.mapic.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    // Find friendship between two users (either direction)
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester.id = :userId1 AND f.addressee.id = :userId2) OR " +
           "(f.requester.id = :userId2 AND f.addressee.id = :userId1)")
    Optional<Friendship> findFriendshipBetween(@Param("userId1") Long userId1, 
                                                @Param("userId2") Long userId2);
    
    // Get all friends (accepted friendships)
    @Query("SELECT f FROM Friendship f WHERE " +
           "(f.requester.id = :userId OR f.addressee.id = :userId) " +
           "AND f.status = :status")
    List<Friendship> findByUserIdAndStatus(@Param("userId") Long userId, 
                                           @Param("status") FriendshipStatus status);
    
    // Get pending requests received by user
    @Query("SELECT f FROM Friendship f WHERE " +
           "f.addressee.id = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingRequestsReceived(@Param("userId") Long userId);
    
    // Get pending requests sent by user
    @Query("SELECT f FROM Friendship f WHERE " +
           "f.requester.id = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingRequestsSent(@Param("userId") Long userId);
    
    // Check if friendship exists between two users
    boolean existsByRequesterAndAddressee(User requester, User addressee);
}

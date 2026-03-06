package com.mapic.backend.repositories;

import com.mapic.backend.entities.OtpToken;
import com.mapic.backend.entities.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    
    Optional<OtpToken> findByEmailAndCodeAndType(String email, String code, OtpType type);
    
    @Query("SELECT COUNT(o) FROM OtpToken o WHERE o.email = :email AND o.type = :type AND o.createdAt > :since")
    long countRecentOtpByEmailAndType(@Param("email") String email, 
                                      @Param("type") OtpType type, 
                                      @Param("since") LocalDateTime since);
    
    @Modifying
    @Query("UPDATE OtpToken o SET o.isUsed = true WHERE o.email = :email AND o.type = :type AND o.isUsed = false")
    void invalidateOldOtps(@Param("email") String email, @Param("type") OtpType type);
    
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :threshold")
    void deleteByExpiresAtBefore(@Param("threshold") LocalDateTime threshold);
}

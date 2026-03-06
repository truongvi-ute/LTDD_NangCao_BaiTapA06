package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class User extends Account {
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true)
    private String phoneNumber;
    
    @Column(nullable = false)
    private Boolean isVerified = false;
    
    @Column(nullable = false)
    private Boolean isBlocked = false;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;
}

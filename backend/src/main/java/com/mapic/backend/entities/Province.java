package com.mapic.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provinces")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Province {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String code; // VN-DN, VN-HN, VN-SG
    
    @Column(nullable = false)
    private String name; // Đà Nẵng, Hà Nội, TP.HCM
    
    @Column(nullable = false)
    private String nameEn; // Da Nang, Hanoi, Ho Chi Minh City
    
    @Column(nullable = false)
    private String region; // Miền Bắc, Miền Trung, Miền Nam
    
    @Column
    private Double latitude; // Tọa độ trung tâm tỉnh
    
    @Column
    private Double longitude;
}

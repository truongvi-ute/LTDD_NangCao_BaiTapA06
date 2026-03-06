package com.mapic.backend.services;

import com.mapic.backend.entities.Gender;
import com.mapic.backend.entities.User;
import com.mapic.backend.entities.UserProfile;
import com.mapic.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private static final String UPLOAD_DIR = "uploads/avatars/";
    
    public static String buildAvatarUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        // If already a full path, return as is
        if (filename.startsWith("http") || filename.startsWith("/uploads/")) {
            return filename;
        }
        return "/uploads/avatars/" + filename;
    }
    
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Update user profile
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        
        // Delete old avatar if exists
        if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
            try {
                Path oldFile = Paths.get(UPLOAD_DIR + profile.getAvatarUrl());
                Files.deleteIfExists(oldFile);
            } catch (IOException e) {
                log.warn("Failed to delete old avatar: {}", e.getMessage());
            }
        }
        
        profile.setAvatarUrl(filename);
        userRepository.save(user);
        
        log.info("Avatar uploaded successfully for user: {}", userId);
        return filename;
    }
    
    @Transactional
    public void updateProfile(Long userId, String name, String bio, String genderStr, 
                             String dateOfBirthStr, String location, String website) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update user name
        user.setName(name);
        
        // Get or create profile
        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        
        // Update profile fields
        profile.setBio(bio != null && !bio.isEmpty() ? bio : null);
        profile.setLocation(location != null && !location.isEmpty() ? location : null);
        profile.setWebsite(website != null && !website.isEmpty() ? website : null);
        
        // Parse and set gender
        if (genderStr != null && !genderStr.isEmpty()) {
            try {
                profile.setGender(Gender.valueOf(genderStr));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid gender value: {}", genderStr);
            }
        } else {
            profile.setGender(null);
        }
        
        // Parse and set date of birth
        if (dateOfBirthStr != null && !dateOfBirthStr.isEmpty()) {
            try {
                profile.setDateOfBirth(java.time.LocalDate.parse(dateOfBirthStr));
            } catch (Exception e) {
                log.warn("Invalid date format: {}", dateOfBirthStr);
            }
        } else {
            profile.setDateOfBirth(null);
        }
        
        userRepository.save(user);
        log.info("Profile updated successfully for user: {}", userId);
    }
    
    @Transactional
    public void updateName(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setName(name);
        userRepository.save(user);
        
        log.info("Name updated successfully for user: {}", userId);
    }
    
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

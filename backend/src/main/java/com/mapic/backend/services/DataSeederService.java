package com.mapic.backend.services;

import com.mapic.backend.entities.*;
import com.mapic.backend.repositories.*;
import com.mapic.backend.utils.ImageDownloader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSeederService implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final MomentRepository momentRepository;
    private final ProvinceRepository provinceRepository;
    private final FriendshipRepository friendshipRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageDownloader imageDownloader;
    private final MomentStatsService momentStatsService;
    
    private final Random random = new Random();
    
    @Override
    @Transactional
    public void run(String... args) {
        // Check if data already exists
        if (userRepository.count() > 5) {
            log.info("Database already seeded. Skipping...");
            return;
        }
        
        log.info("Starting database seeding...");
        
        try {
            List<Province> provinces = provinceRepository.findAll();
            if (provinces.isEmpty()) {
                log.warn("No provinces found. Please ensure provinces are loaded first.");
                return;
            }
            
            // Get HCM and HN provinces
            Province hcm = findProvinceByCode(provinces, "VN-SG");
            Province hanoi = findProvinceByCode(provinces, "VN-HN");
            
            if (hcm == null || hanoi == null) {
                log.error("Could not find HCM or Hanoi provinces");
                return;
            }
            
            List<User> users = createUsers();
            createMomentsForUsers(users, hcm, hanoi);
            createFriendships(users);
            
            // Recalculate all stats from actual database data
            log.info("Recalculating moment statistics...");
            momentStatsService.updateAllMomentStats();
            
            log.info("Database seeding completed successfully!");
            log.info("Created {} users with 5 moments each", users.size());
        } catch (Exception e) {
            log.error("Error during database seeding: ", e);
        }
    }
    
    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        
        String[] vietnameseNames = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Hoàng Cường", 
            "Phạm Thu Dung", "Hoàng Minh Đức", "Vũ Thị Hà",
            "Đặng Quốc Huy", "Bùi Thanh Lan", "Phan Văn Long", 
            "Đỗ Thị Mai"
        };
        
        for (int i = 0; i < 10; i++) {
            String username = "user" + (i + 1);
            String fullName = vietnameseNames[i];
            
            User user = new User();
            user.setUsername(username);
            user.setEmail(username + "@mapic.vn");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setName(fullName);
            user.setStatus(AccountStatus.ACTIVE);
            user.setIsVerified(true);
            user.setIsBlocked(false);
            
            user = userRepository.save(user);
            
            // Create profile
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            profile.setBio(generateBio());
            profile.setGender(i % 3 == 0 ? Gender.MALE : i % 3 == 1 ? Gender.FEMALE : Gender.OTHER);
            profile.setDateOfBirth(LocalDate.of(1990 + (i % 10), (i % 12) + 1, (i % 28) + 1));
            profile.setLocation(i < 5 ? "TP. Hồ Chí Minh" : "Hà Nội");
            
            userProfileRepository.save(profile);
            
            users.add(user);
            log.info("Created user: {} ({})", fullName, username);
        }
        
        return users;
    }
    
    private Province findProvinceByCode(List<Province> provinces, String code) {
        return provinces.stream()
                .filter(p -> p.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

    private void createMomentsForUsers(List<User> users, Province hcm, Province hanoi) {
        log.info("Creating moments for users...");
        
        // All categories to distribute evenly
        MomentCategory[] categories = MomentCategory.values();
        
        // Captions for each category
        String[][] captions = {
            {"Phong cảnh tuyệt đẹp 🏞️", "Thiên nhiên hùng vĩ 🌄", "Cảnh đẹp mê hồn 🌅"},
            {"Khoảnh khắc đáng nhớ 👥", "Cùng bạn bè 🤝", "Gia đình sum vầy 👨‍👩‍👧‍👦"},
            {"Món ngon khó cưỡng 🍜", "Ẩm thực đường phố 🍲", "Bữa ăn ngon miệng 🍱"},
            {"Kiến trúc độc đáo 🏛️", "Công trình ấn tượng 🏗️", "Kiến trúc cổ kính 🏰"},
            {"Văn hóa truyền thống 🎭", "Lễ hội sôi động 🎪", "Di sản văn hóa 🏮"},
            {"Thiên nhiên hoang dã 🌿", "Rừng xanh mát mẻ 🌳", "Hòa mình vào thiên nhiên 🦋"},
            {"Thành phố sôi động 🏙️", "Nhịp sống đô thị 🌃", "Phố phường tấp nập 🚦"},
            {"Sự kiện đặc biệt 🎉", "Lễ kỷ niệm 🎊", "Hoạt động cộng đồng 🎈"},
            {"Khoảnh khắc thú vị 📌", "Trải nghiệm mới 🎯", "Điều đặc biệt ✨"}
        };
        
        // Locations in HCM and Hanoi
        String[][] hcmLocations = {
            {"Nhà thờ Đức Bà", "10.779965", "106.699092"},
            {"Bến Nhà Rồng", "10.767240", "106.706200"},
            {"Chợ Bến Thành", "10.772431", "106.698212"},
            {"Phố đi bộ Nguyễn Huệ", "10.774572", "106.703909"},
            {"Công viên Tao Đàn", "10.782932", "106.692116"}
        };
        
        String[][] hanoiLocations = {
            {"Hồ Hoàn Kiếm", "21.028969", "105.852182"},
            {"Văn Miếu Quốc Tử Giám", "21.027764", "105.835342"},
            {"Phố cổ Hà Nội", "21.036018", "105.849571"},
            {"Lăng Chủ tịch Hồ Chí Minh", "21.037086", "105.834511"},
            {"Chùa Một Cột", "21.036018", "105.833571"}
        };
        
        int momentIndex = 0;
        
        for (int userIdx = 0; userIdx < users.size(); userIdx++) {
            User user = users.get(userIdx);
            Province province = userIdx < 5 ? hcm : hanoi;
            String[][] locations = userIdx < 5 ? hcmLocations : hanoiLocations;
            
            for (int i = 0; i < 5; i++) {
                // Distribute categories evenly across all moments
                MomentCategory category = categories[momentIndex % categories.length];
                int categoryIndex = momentIndex % categories.length;
                String caption = captions[categoryIndex][momentIndex % captions[categoryIndex].length];
                
                // Get location
                String[] location = locations[i % locations.length];
                String locationName = location[0];
                double latitude = Double.parseDouble(location[1]);
                double longitude = Double.parseDouble(location[2]);
                
                // Add small random offset
                latitude += (random.nextDouble() - 0.5) * 0.01;
                longitude += (random.nextDouble() - 0.5) * 0.01;
                
                // Download image from placeholder service
                String placeholderUrl = imageDownloader.getPlaceholderImageUrl(momentIndex);
                String tempFilename = "moment-" + momentIndex + ".jpg";
                String downloadedFilename = imageDownloader.downloadImage(placeholderUrl, tempFilename);
                
                // If download failed, skip this moment
                if (downloadedFilename == null) {
                    log.warn("Skipping moment {} due to image download failure", momentIndex);
                    momentIndex++;
                    continue;
                }
                
                Moment moment = new Moment();
                moment.setAuthor(user);
                moment.setImageUrl(downloadedFilename);
                moment.setCaption(caption);
                moment.setLatitude(latitude);
                moment.setLongitude(longitude);
                moment.setAddressName(locationName + ", " + province.getName());
                moment.setIsPublic(true);
                moment.setCategory(category);
                moment.setStatus(MomentStatus.ACTIVE);
                moment.setProvince(province);
                moment.setReactionCount(0L);
                moment.setCommentCount(0L);
                moment.setSaveCount(0L);
                
                momentRepository.save(moment);
                log.info("Created moment {} for user {} at {} ({})", 
                        momentIndex + 1, user.getName(), locationName, category);
                
                momentIndex++;
            }
        }
        
        log.info("Created {} moments total", momentIndex);
    }
    
    private void createFriendships(List<User> users) {
        int friendshipCount = 0;
        
        for (int i = 0; i < users.size(); i++) {
            // Create 3-5 friendships per user
            int numFriends = 3 + random.nextInt(3);
            
            for (int j = 0; j < numFriends; j++) {
                int friendIndex = (i + j + 1) % users.size();
                if (friendIndex == i) continue;
                
                User requester = users.get(i);
                User addressee = users.get(friendIndex);
                
                // Check if friendship already exists
                boolean exists = friendshipRepository.existsByRequesterAndAddressee(requester, addressee) ||
                                friendshipRepository.existsByRequesterAndAddressee(addressee, requester);
                
                if (!exists) {
                    Friendship friendship = new Friendship();
                    friendship.setRequester(requester);
                    friendship.setAddressee(addressee);
                    friendship.setStatus(FriendshipStatus.ACCEPTED);
                    
                    friendshipRepository.save(friendship);
                    friendshipCount++;
                }
            }
        }
        
        log.info("Created {} friendships", friendshipCount);
    }
    
    private String generateBio() {
        String[] bios = {
            "Yêu du lịch và khám phá Việt Nam 🇻🇳",
            "Nhiếp ảnh phong cảnh | Travel blogger",
            "Đi để trở về ✈️ Sống để khám phá 🌏",
            "Foodie & Travel enthusiast 🍜",
            "Capturing beautiful moments 📸",
            "Khám phá vẻ đẹp Việt Nam 🏞️",
            "Adventure seeker | Nature lover 🌿",
            "Life is a journey, not a destination 🚶",
            "Passionate about Vietnamese culture 🎭",
            "Making memories around Vietnam 💚"
        };
        return bios[random.nextInt(bios.length)];
    }
}

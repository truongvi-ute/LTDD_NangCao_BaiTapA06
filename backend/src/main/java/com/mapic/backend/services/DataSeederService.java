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
            
            List<User> users = createUsers();
            createMomentsForUsers(users, provinces);
            createFriendships(users);
            
            // Recalculate all stats from actual database data
            log.info("Recalculating moment statistics...");
            momentStatsService.updateAllMomentStats();
            
            log.info("Database seeding completed successfully!");
            log.info("Created {} users with {} moments each", users.size(), 5);
        } catch (Exception e) {
            log.error("Error during database seeding: ", e);
        }
    }
    
    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        
        String[] firstNames = {"Minh", "Hương", "Tuấn", "Linh", "Khoa", "Phương", "Đức", "Hà", "Nam", "Trang",
                               "Hoàng", "Mai", "Quân", "Thảo", "Bảo", "Ngọc", "Hải", "Lan", "Việt", "Anh"};
        String[] lastNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Phan", "Vũ", "Đặng", "Bùi", "Đỗ"};
        
        for (int i = 0; i < 20; i++) {
            String firstName = firstNames[i];
            String lastName = lastNames[i % 10];
            String fullName = lastName + " " + firstName;
            String username = firstName.toLowerCase() + (i + 1);
            
            User user = new User();
            user.setUsername(username);
            user.setEmail(username + "@mapic.vn");
            user.setPassword(passwordEncoder.encode("password123"));
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
            profile.setLocation(getRandomLocation());
            
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

    private void createMomentsForUsers(List<User> users, List<Province> provinces) {
        int momentIndex = 0;
        
        log.info("Downloading sample images...");
        
        for (User user : users) {
            for (int i = 0; i < 5; i++) {
                MomentData momentData = getMomentData(momentIndex);
                
                // Find the correct province for this moment
                Province province = null;
                if (momentData.provinceCode != null) {
                    province = findProvinceByCode(provinces, momentData.provinceCode);
                }
                // Fallback to random if not found
                if (province == null) {
                    province = provinces.get(random.nextInt(provinces.size()));
                }
                
                // Download image from placeholder service
                String imageUrl = momentData.imageUrl;
                String placeholderUrl = imageDownloader.getPlaceholderImageUrl(momentIndex);
                String downloadedFilename = imageDownloader.downloadImage(placeholderUrl, imageUrl);
                
                // Use downloaded filename or fallback to original
                if (downloadedFilename == null) {
                    log.warn("Failed to download image for moment {}, using filename only", momentIndex);
                    downloadedFilename = imageUrl;
                }
                
                Moment moment = new Moment();
                moment.setAuthor(user);
                moment.setImageUrl(downloadedFilename);
                moment.setCaption(momentData.caption);
                moment.setLatitude(province.getLatitude() + (random.nextDouble() - 0.5) * 0.1);
                moment.setLongitude(province.getLongitude() + (random.nextDouble() - 0.5) * 0.1);
                String addressName = (momentData.provinceCode != null)
                        ? momentData.address + ", " + province.getName()
                        : momentData.address;
                moment.setAddressName(addressName);
                moment.setIsPublic(random.nextBoolean());
                moment.setCategory(getRandomCategory());
                moment.setProvince(province);
                moment.setStatus(MomentStatus.ACTIVE);
                // Initialize counts to 0 - will be calculated from actual data
                moment.setReactionCount(0L);
                moment.setCommentCount(0L);
                moment.setSaveCount(0L);
                
                momentRepository.save(moment);
                momentIndex++;
            }
            log.info("Created 5 moments for user: {}", user.getName());
        }
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
    
    private String getRandomLocation() {
        String[] locations = {
            "Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ",
            "Nha Trang", "Huế", "Đà Lạt", "Vũng Tàu", "Hội An"
        };
        return locations[random.nextInt(locations.length)];
    }
    
    private MomentCategory getRandomCategory() {
        MomentCategory[] categories = MomentCategory.values();
        return categories[random.nextInt(categories.length)];
    }
    
    private MomentData getMomentData(int index) {
        // Using placeholder images - in production, these should be actual uploaded images
        MomentData[] moments = {
            // Hà Nội
            new MomentData("hanoi-oldquarter.jpg", "Phố cổ Hà Nội về đêm 🏮", "Phố cổ Hà Nội", "VN-HN"),
            new MomentData("hoankiemlake.jpg", "Hồ Gươm buổi sáng sớm 🌅", "Hồ Hoàn Kiếm", "VN-HN"),
            new MomentData("temple-literature.jpg", "Văn Miếu - Quốc Tử Giám 📚", "Văn Miếu", "VN-HN"),
            new MomentData("tran-quoc-pagoda.jpg", "Chùa Trấn Quốc bên Hồ Tây 🛕", "Hồ Tây", "VN-HN"),
            new MomentData("hanoi-food.jpg", "Bún chả Hà Nội 🥢", "Hà Nội", "VN-HN"),

            // Quảng Ninh (Vịnh Hạ Long)
            new MomentData("halong-bay-1.jpg", "Vịnh Hạ Long tuyệt đẹp 🌊", "Vịnh Hạ Long", "VN-QN"),
            new MomentData("cat-ba.jpg", "Đảo Cát Bà 🏝️", "Đảo Cát Bà", "VN-QN"),
            new MomentData("ha-long.jpg", "Vịnh Hạ Long 🚢", "Hạ Long", "VN-QN"),

            // Đà Nẵng
            new MomentData("danang-bridge.jpg", "Cầu Rồng phun lửa 🐉", "Cầu Rồng", "VN-DN"),
            new MomentData("bana-hills.jpg", "Bà Nà Hills - Cầu Vàng ✋", "Bà Nà Hills", "VN-DN"),
            new MomentData("my-khe-beach.jpg", "Biển Mỹ Khê tuyệt đẹp 🏖️", "Biển Mỹ Khê", "VN-DN"),
            new MomentData("marble-mountains.jpg", "Ngũ Hành Sơn hùng vĩ ⛰️", "Ngũ Hành Sơn", "VN-DN"),
            new MomentData("son-tra.jpg", "Bán đảo Sơn Trà 🌴", "Bán đảo Sơn Trà", "VN-DN"),
            new MomentData("danang-food.jpg", "Mì Quảng Đà Nẵng 🍜", "Đà Nẵng", "VN-DN"),

            // TP.HCM
            new MomentData("saigon-cathedral.jpg", "Nhà thờ Đức Bà Sài Gòn ⛪", "Nhà thờ Đức Bà", "VN-SG"),
            new MomentData("ben-thanh-market.jpg", "Chợ Bến Thành nhộn nhịp 🏪", "Chợ Bến Thành", "VN-SG"),
            new MomentData("bitexco-tower.jpg", "Tòa nhà Bitexco về đêm 🌃", "Bitexco Financial Tower", "VN-SG"),
            new MomentData("saigon-river.jpg", "Sông Sài Gòn lãng mạn 🌉", "Sông Sài Gòn", "VN-SG"),
            new MomentData("independence-palace.jpg", "Dinh Độc Lập lịch sử 🏛️", "Dinh Độc Lập", "VN-SG"),
            new MomentData("saigon-food.jpg", "Phở Sài Gòn 🍲", "TP. Hồ Chí Minh", "VN-SG"),

            // Thừa Thiên Huế
            new MomentData("hue-citadel.jpg", "Đại Nội Huế cổ kính 🏯", "Đại Nội Huế", "VN-TT"),
            new MomentData("thien-mu-pagoda.jpg", "Chùa Thiên Mụ bên sông Hương 🛕", "Chùa Thiên Mụ", "VN-TT"),
            new MomentData("perfume-river.jpg", "Sông Hương thơ mộng 🚣", "Sông Hương", "VN-TT"),
            new MomentData("hue-royal-tomb.jpg", "Lăng Khải Định 👑", "Lăng Khải Định", "VN-TT"),
            new MomentData("hue-bridge.jpg", "Cầu Trường Tiền về đêm 🌉", "Cầu Trường Tiền", "VN-TT"),
            new MomentData("hue-food.jpg", "Ẩm thực Huế 🍜", "Huế", "VN-TT"),

            // Khánh Hòa (Nha Trang)
            new MomentData("nhatrang-beach.jpg", "Biển Nha Trang xanh ngắt 🏝️", "Bãi biển Nha Trang", "VN-KH"),
            new MomentData("vinpearl-nhatrang.jpg", "Vinpearl Land Nha Trang 🎢", "Vinpearl Nha Trang", "VN-KH"),
            new MomentData("ponagar-tower.jpg", "Tháp Bà Ponagar 🗼", "Tháp Bà", "VN-KH"),
            new MomentData("hon-chong.jpg", "Hòn Chồng - Vườn đá 🪨", "Hòn Chồng", "VN-KH"),
            new MomentData("nhatrang-night.jpg", "Nha Trang về đêm 🌃", "Trung tâm Nha Trang", "VN-KH"),
            new MomentData("nhatrang-food.jpg", "Hải sản Nha Trang 🦞", "Nha Trang", "VN-KH"),

            // Lâm Đồng (Đà Lạt)
            new MomentData("dalat-flower.jpg", "Thành phố ngàn hoa 🌸", "Đà Lạt", "VN-LĐ"),
            new MomentData("xuan-huong-lake.jpg", "Hồ Xuân Hương lãng mạn 💕", "Hồ Xuân Hương", "VN-LĐ"),
            new MomentData("dalat-railway.jpg", "Ga xe lửa Đà Lạt cổ 🚂", "Ga Đà Lạt", "VN-LĐ"),
            new MomentData("crazy-house.jpg", "Crazy House độc đáo 🏠", "Crazy House", "VN-LĐ"),
            new MomentData("langbiang.jpg", "Đỉnh Langbiang hùng vĩ ⛰️", "Langbiang", "VN-LĐ"),
            new MomentData("dalat-food.jpg", "Bánh tráng nướng Đà Lạt 🫓", "Đà Lạt", "VN-LĐ"),

            // Quảng Nam (Hội An)
            new MomentData("hoian-ancient.jpg", "Phố cổ Hội An 🏮", "Phố cổ Hội An", "VN-QNM"),
            new MomentData("japanese-bridge.jpg", "Chùa Cầu Hội An 🌉", "Chùa Cầu", "VN-QNM"),
            new MomentData("hoian-lantern.jpg", "Đêm hoa đăng Hội An ✨", "Hội An", "VN-QNM"),
            new MomentData("an-bang-beach.jpg", "Biển An Bảng 🏖️", "Biển An Bảng", "VN-QNM"),
            new MomentData("hoian-market.jpg", "Chợ Hội An 🛍️", "Chợ Hội An", "VN-QNM"),
            new MomentData("hoi-an-food.jpg", "Cao lầu Hội An 🍝", "Hội An", "VN-QNM"),

            // Kiên Giang (Phú Quốc)
            new MomentData("phuquoc-beach.jpg", "Bãi Sao Phú Quốc 🌴", "Bãi Sao", "VN-KG"),
            new MomentData("phuquoc-sunset.jpg", "Hoàng hôn Phú Quốc 🌅", "Phú Quốc", "VN-KG"),
            new MomentData("vinpearl-phuquoc.jpg", "Vinpearl Safari 🦁", "Vinpearl Phú Quốc", "VN-KG"),
            new MomentData("phuquoc-night-market.jpg", "Chợ đêm Phú Quốc 🌙", "Chợ đêm Phú Quốc", "VN-KG"),
            new MomentData("phuquoc-cable-car.jpg", "Cáp treo Hòn Thơm 🚡", "Cáp treo Hòn Thơm", "VN-KG"),

            // Lào Cai (Sapa) - dùng Hà Giang thay vì không có Lào Cai
            new MomentData("sapa-terrace.jpg", "Ruộng bậc thang Sapa 🌾", "Sapa", null),
            new MomentData("fansipan.jpg", "Đỉnh Fansipan - Nóc nhà Đông Dương 🏔️", "Fansipan, Sapa", null),
            new MomentData("sapa-market.jpg", "Chợ phiên Sapa 🎪", "Chợ Sapa", null),
            new MomentData("sapa-village.jpg", "Bản làng Sapa 🏘️", "Bản làng Sapa", null),
            new MomentData("sapa-cloud.jpg", "Săn mây Sapa ☁️", "Sapa", null),

            // Cần Thơ (Mekong Delta)
            new MomentData("mekong-river.jpg", "Sông Mekong bao la 🚤", "Sông Mekong", "VN-CT"),
            new MomentData("floating-market.jpg", "Chợ nổi Cái Răng 🛶", "Chợ nổi Cái Răng", "VN-CT"),
            new MomentData("cantho-bridge.jpg", "Cầu Cần Thơ 🌉", "Cầu Cần Thơ", "VN-CT"),
            new MomentData("mekong-garden.jpg", "Vườn trái cây miệt vườn 🍊", "Miệt vườn Mekong", null),
            new MomentData("mekong-coconut.jpg", "Rừng dừa Bảy Mẫu 🥥", "Rừng dừa", null),

            // Ninh Bình
            new MomentData("ninh-binh.jpg", "Tràng An - Ninh Bình 🚣", "Tràng An", "VN-NB"),
            new MomentData("tam-coc.jpg", "Tam Cốc - Bích Động 🛶", "Tam Cốc", "VN-NB"),

            // Bình Thuận (Mũi Né)
            new MomentData("mui-ne.jpg", "Đồi cát Mũi Né 🏜️", "Mũi Né", "VN-BT"),
            new MomentData("phan-thiet.jpg", "Phan Thiết - Bình Thuận 🏖️", "Phan Thiết", "VN-BT"),

            // Bình Định (Quy Nhơn)
            new MomentData("quy-nhon.jpg", "Bãi Xép - Quy Nhơn 🌊", "Quy Nhơn", "VN-BD"),

            // Phú Yên
            new MomentData("phu-yen.jpg", "Gành Đá Đĩa - Phú Yên 🪨", "Gành Đá Đĩa", "VN-PY"),

            // Bà Rịa - Vũng Tàu
            new MomentData("vung-tau.jpg", "Tượng Chúa Kitô - Vũng Tàu ⛪", "Vũng Tàu", "VN-BR"),
            new MomentData("con-dao.jpg", "Côn Đảo hoang sơ 🏝️", "Côn Đảo", "VN-BR"),

            // Ẩm thực Việt Nam (không gắn tỉnh cụ thể)
            new MomentData("vietnam-coffee.jpg", "Cà phê Việt Nam ☕", "Việt Nam", null),
            new MomentData("banh-mi.jpg", "Bánh mì Việt Nam 🥖", "Việt Nam", null),
            new MomentData("spring-roll.jpg", "Gỏi cuốn tươi ngon 🥗", "Việt Nam", null),
            new MomentData("pho.jpg", "Phở Việt Nam 🍜", "Việt Nam", null),
            new MomentData("vietnamese-culture.jpg", "Văn hóa Việt Nam 🎭", "Việt Nam", null)
        };
        
        return moments[index % moments.length];
    }
    
    private static class MomentData {
        String imageUrl;
        String caption;
        String address;
        String provinceCode; // matches codes in data.sql
        
        MomentData(String imageUrl, String caption, String address, String provinceCode) {
            this.imageUrl = imageUrl;
            this.caption = caption;
            this.address = address;
            this.provinceCode = provinceCode;
        }
    }
}

package com.mapic.backend.services;

import com.mapic.backend.entities.*;
import com.mapic.backend.repositories.*;
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
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final MomentStatsService momentStatsService;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        // Check if data already exists
        if (userRepository.count() >= 20) {
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
            List<Moment> moments = createMomentsForUsers(users, provinces);
            createFriendships(users);
            createCommentsAndReactions(moments, users);

            // Recalculate all stats from actual database data
            log.info("Recalculating moment statistics...");
            momentStatsService.updateAllMomentStats();

            log.info("Database seeding completed successfully!");
            log.info("Created {} users", users.size());
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
                "Đỗ Thị Mai", "Lê Thị Ngọc", "Ngô Văn Khải",
                "Hồ Quỳnh Hương", "Đinh Tấn Phát", "Võ Thị Lệ",
                "Trịnh Văn Công", "Lý Thảo Nhi", "Bùi Kiều Oanh",
                "Trần Minh Phúc", "Nguyễn Ngọc Nga"
        };

        for (int i = 0; i < 20; i++) {
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
            profile.setLocation(i < 10 ? "TP. Hồ Chí Minh" : "Hà Nội");

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

    private List<Moment> createMomentsForUsers(List<User> users, List<Province> provinces) {
        log.info("Creating moments for users from seed_data.json...");
        List<Moment> createdMoments = new ArrayList<>();

        try {
            // Read JSON file
            java.nio.file.Path jsonPath = java.nio.file.Paths.get("uploads/moments/seed_data.json");
            if (!java.nio.file.Files.exists(jsonPath)) {
                log.warn("seed_data.json not found. Run the python script first.");
                return createdMoments;
            }

            String jsonContent = new String(java.nio.file.Files.readAllBytes(jsonPath),
                    java.nio.charset.StandardCharsets.UTF_8);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootArray = mapper.readTree(jsonContent);

            int momentIndex = 0;
            for (com.fasterxml.jackson.databind.JsonNode node : rootArray) {
                String filename = node.get("filename").asText();
                String categoryStr = node.get("category").asText();
                String provinceCode = node.get("provinceCode").asText();
                String provinceName = node.get("provinceName").asText();
                String addressName = node.get("addressName").asText();
                String caption = node.get("caption").asText();
                double latitude = node.get("latitude").asDouble();
                double longitude = node.get("longitude").asDouble();

                // Find province entity
                Province province = findProvinceByCode(provinces, provinceCode);
                if (province == null) {
                    log.warn("Could not find province with code {} for image {}", provinceCode, filename);
                    continue; // Skip if province doesn't exist in DB
                }

                // Assign to a random user
                User user = users.get(random.nextInt(users.size()));

                MomentCategory category;
                try {
                    category = MomentCategory.valueOf(categoryStr);
                } catch (IllegalArgumentException e) {
                    category = MomentCategory.OTHER;
                }

                Moment moment = new Moment();
                moment.setAuthor(user);
                moment.setImageUrl(filename);
                moment.setCaption(caption);
                moment.setLatitude(latitude);
                moment.setLongitude(longitude);
                moment.setAddressName(addressName);
                moment.setIsPublic(true);
                moment.setCategory(category);
                moment.setStatus(MomentStatus.ACTIVE);
                moment.setProvince(province);
                moment.setReactionCount(0L);
                moment.setCommentCount(0L);
                moment.setSaveCount(0L);

                moment = momentRepository.save(moment);
                createdMoments.add(moment);
                momentIndex++;
            }
            log.info("Successfully created {} moments from seed data", momentIndex);

        } catch (Exception e) {
            log.error("Failed to parse seed_data.json: {}", e.getMessage(), e);
        }
        return createdMoments;
    }

    private void createFriendships(List<User> users) {
        int friendshipCount = 0;

        for (int i = 0; i < users.size(); i++) {
            // Create 3-5 friendships per user
            int numFriends = 3 + random.nextInt(3);

            for (int j = 0; j < numFriends; j++) {
                int friendIndex = (i + j + 1) % users.size();
                if (friendIndex == i)
                    continue;

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

    private void createCommentsAndReactions(List<Moment> moments, List<User> users) {
        log.info("Creating comments and reactions...");
        int commentCount = 0;
        int reactionCount = 0;

        String[] sampleComments = {
                "Ảnh đẹp quá! 😍", "Tuyệt vời!", "Chỗ này ở đâu vậy bạn?",
                "Cảnh xuất sắc quá luôn 👏", "Mình cũng muốn đến đây một lần",
                "Wow, thật ấn tượng! 😮", "Góc chụp đẹp quá bạn ơi",
                "Đỉnh của chóp 🔥", "Thiên nhiên hùng vĩ thật", "Rất thích bầu không khí này",
                "Đẹp mê li 💖", "Việt Nam mình đẹp quá", "Cho xin địa chỉ cụ thể đi ạ",
                "10 điểm không có nhưng 💯", "Quá đã!"
        };

        for (Moment moment : moments) {
            // Tương tác ngẫu nhiên cho mỗi moment
            int numReactions = 3 + random.nextInt(10); // 3-12 reactions
            int numComments = 1 + random.nextInt(5); // 1-5 comments

            // Lấy danh sách user tham gia tương tác ngẫu nhiên
            List<User> interactors = new ArrayList<>(users);
            java.util.Collections.shuffle(interactors);

            // Tạo reactions
            for (int i = 0; i < Math.min(numReactions, interactors.size()); i++) {
                User user = interactors.get(i);
                Reaction reaction = new Reaction();
                reaction.setMoment(moment);
                reaction.setUser(user);

                // 60% LIKE, 30% LOVE, 10% WOW
                int rand = random.nextInt(100);
                ReactionType type = (rand < 60) ? ReactionType.LIKE
                        : (rand < 90) ? ReactionType.LOVE : ReactionType.WOW;
                reaction.setType(type);

                reactionRepository.save(reaction);
                reactionCount++;
            }

            // Xáo trộn lại user để comment
            java.util.Collections.shuffle(interactors);

            // Tạo comments
            for (int i = 0; i < Math.min(numComments, interactors.size()); i++) {
                User user = interactors.get(i);
                Comment comment = new Comment();
                comment.setMoment(moment);
                comment.setUser(user);
                comment.setContent(sampleComments[random.nextInt(sampleComments.length)]);
                comment.setIsBlocked(false);

                commentRepository.save(comment);
                commentCount++;
            }
        }

        log.info("Created {} reactions and {} comments", reactionCount, commentCount);
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

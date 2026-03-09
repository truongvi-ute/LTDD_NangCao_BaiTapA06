# Quick Start - MAPIC Backend

## Khởi Động Nhanh

### 1. Chuẩn Bị Database

Đảm bảo PostgreSQL đang chạy và tạo database:

```sql
CREATE DATABASE mapic_db;
```

### 2. Cấu Hình

Kiểm tra file `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mapic_db
spring.datasource.username=postgres
spring.datasource.password=123
```

### 3. Chạy Backend

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 4. Tự Động Seed Data

Khi backend khởi động lần đầu, nó sẽ tự động:

1. ✅ Tạo 45 tỉnh thành Việt Nam
2. ✅ Tạo 20 users (username: minh1, huong2, ..., anh20)
3. ✅ Tạo 100 moments với ảnh tự động download
4. ✅ Tạo friendships giữa users

**Lưu ý:** Quá trình download 100 ảnh có thể mất 2-3 phút. Hãy kiên nhẫn!

### 5. Test Login

Sau khi seeding xong, bạn có thể login với:

- **Username:** `minh1`, `huong2`, `tuan3`, ..., `anh20`
- **Password:** `password123` (tất cả users)

### 6. API Endpoints

Backend chạy tại: `http://localhost:8080`

#### Test APIs:

```bash
# Get all provinces
curl http://localhost:8080/api/provinces

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"minh1","password":"password123"}'

# Get feed (cần JWT token)
curl http://localhost:8080/api/moments/feed \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Xử Lý Sự Cố

### Lỗi: "Database already seeded"

Nếu muốn seed lại:

```sql
-- Xóa dữ liệu cũ
DELETE FROM friendships;
DELETE FROM moments;
DELETE FROM user_profiles;
DELETE FROM users;
```

Hoặc gọi API:

```bash
curl -X POST http://localhost:8080/api/admin/seed-database
```

### Lỗi: "Failed to download image"

- Kiểm tra kết nối internet
- Ảnh sẽ được download từ picsum.photos
- Nếu download thất bại, dữ liệu vẫn được tạo (chỉ ảnh không hiển thị)

### Lỗi: "No provinces found"

Kiểm tra file `data.sql` đã được execute:

```properties
# Trong application.properties
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

## Kiểm Tra Dữ Liệu

```sql
-- Check users
SELECT COUNT(*) FROM users;  -- Should be 20

-- Check moments
SELECT COUNT(*) FROM moments;  -- Should be 100

-- Check images downloaded
SELECT COUNT(*) FROM moments WHERE image_url IS NOT NULL;

-- Check moments by province
SELECT p.name, COUNT(m.id) as moment_count
FROM provinces p
LEFT JOIN moments m ON m.province_id = p.id
GROUP BY p.name
ORDER BY moment_count DESC;
```

## Thư Mục Ảnh

Ảnh được lưu tại: `backend/uploads/moments/`

Bạn có thể:
- Xem ảnh đã download
- Thêm ảnh thật vào thư mục này
- Thay thế ảnh placeholder

## Tắt Auto-Seeding

Nếu không muốn auto-seed, comment out annotation:

```java
// @Service  // Comment this line
public class DataSeederService implements CommandLineRunner {
    // ...
}
```

## Các Tính Năng Đã Seed

✅ 20 Users với profiles đầy đủ
✅ 100 Moments với ảnh thật
✅ 45 Provinces Việt Nam
✅ 60-80 Friendships
✅ Reaction counts, comment counts
✅ Public/Private moments
✅ Đa dạng categories

## Next Steps

1. Start frontend: `cd frontend && npm start`
2. Login với user đã seed
3. Xem feed với 100 moments
4. Test các tính năng social
5. Thêm moments mới

## Support

Nếu gặp vấn đề, check logs:

```bash
# Logs sẽ hiển thị:
# - "Starting database seeding..."
# - "Created user: Nguyễn Minh (minh1)"
# - "Downloaded image: halong-bay-1.jpg"
# - "Created 5 moments for user: Nguyễn Minh"
# - "Database seeding completed successfully!"
```

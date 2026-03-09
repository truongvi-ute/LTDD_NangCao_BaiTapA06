-- Insert Vietnam provinces data
-- This will be executed automatically by Spring Boot on startup

INSERT INTO provinces (code, name, name_en, region, latitude, longitude) VALUES
-- Miền Bắc
('VN-HN', 'Hà Nội', 'Hanoi', 'Miền Bắc', 21.0285, 105.8542),
('VN-HP', 'Hải Phòng', 'Hai Phong', 'Miền Bắc', 20.8449, 106.6881),
('VN-QN', 'Quảng Ninh', 'Quang Ninh', 'Miền Bắc', 21.0064, 107.2925),
('VN-BN', 'Bắc Ninh', 'Bac Ninh', 'Miền Bắc', 21.1861, 106.0763),
('VN-HD', 'Hải Dương', 'Hai Duong', 'Miền Bắc', 20.9373, 106.3148),
('VN-HY', 'Hưng Yên', 'Hung Yen', 'Miền Bắc', 20.6464, 106.0511),
('VN-TB', 'Thái Bình', 'Thai Binh', 'Miền Bắc', 20.4464, 106.3365),
('VN-NĐ', 'Nam Định', 'Nam Dinh', 'Miền Bắc', 20.4388, 106.1621),
('VN-NB', 'Ninh Bình', 'Ninh Binh', 'Miền Bắc', 20.2506, 105.9745),

-- Miền Trung
('VN-TH', 'Thanh Hóa', 'Thanh Hoa', 'Miền Trung', 19.8067, 105.7851),
('VN-NA', 'Nghệ An', 'Nghe An', 'Miền Trung', 19.2342, 104.9200),
('VN-HT', 'Hà Tĩnh', 'Ha Tinh', 'Miền Trung', 18.3559, 105.9069),
('VN-QB', 'Quảng Bình', 'Quang Binh', 'Miền Trung', 17.4676, 106.6222),
('VN-QT', 'Quảng Trị', 'Quang Tri', 'Miền Trung', 16.7943, 107.1856),
('VN-TT', 'Thừa Thiên Huế', 'Thua Thien Hue', 'Miền Trung', 16.4637, 107.5909),
('VN-DN', 'Đà Nẵng', 'Da Nang', 'Miền Trung', 16.0544, 108.2022),
('VN-QNM', 'Quảng Nam', 'Quang Nam', 'Miền Trung', 15.5394, 108.0191),
('VN-QNG', 'Quảng Ngãi', 'Quang Ngai', 'Miền Trung', 15.1214, 108.8044),
('VN-BD', 'Bình Định', 'Binh Dinh', 'Miền Trung', 13.7830, 109.2196),
('VN-PY', 'Phú Yên', 'Phu Yen', 'Miền Trung', 13.0881, 109.0929),
('VN-KH', 'Khánh Hòa', 'Khanh Hoa', 'Miền Trung', 12.2585, 109.0526),

-- Tây Nguyên
('VN-KT', 'Kon Tum', 'Kon Tum', 'Tây Nguyên', 14.3497, 108.0005),
('VN-GL', 'Gia Lai', 'Gia Lai', 'Tây Nguyên', 13.9833, 108.0000),
('VN-DL', 'Đắk Lắk', 'Dak Lak', 'Tây Nguyên', 12.7100, 108.2378),
('VN-DN2', 'Đắk Nông', 'Dak Nong', 'Tây Nguyên', 12.2646, 107.6098),
('VN-LĐ', 'Lâm Đồng', 'Lam Dong', 'Tây Nguyên', 11.5753, 108.1429),

-- Miền Nam
('VN-NT', 'Ninh Thuận', 'Ninh Thuan', 'Miền Nam', 11.6739, 108.8629),
('VN-BT', 'Bình Thuận', 'Binh Thuan', 'Miền Nam', 10.9273, 108.1017),
('VN-SG', 'TP. Hồ Chí Minh', 'Ho Chi Minh City', 'Miền Nam', 10.8231, 106.6297),
('VN-BR', 'Bà Rịa - Vũng Tàu', 'Ba Ria - Vung Tau', 'Miền Nam', 10.5417, 107.2429),
('VN-ĐN', 'Đồng Nai', 'Dong Nai', 'Miền Nam', 10.9500, 107.1667),
('VN-BĐ', 'Bình Dương', 'Binh Duong', 'Miền Nam', 11.3254, 106.4770),
('VN-BP', 'Bình Phước', 'Binh Phuoc', 'Miền Nam', 11.7511, 106.7234),
('VN-TN', 'Tây Ninh', 'Tay Ninh', 'Miền Nam', 11.3351, 106.1098),
('VN-LAN', 'Long An', 'Long An', 'Miền Nam', 10.6957, 106.2431),
('VN-TG', 'Tiền Giang', 'Tien Giang', 'Miền Nam', 10.4493, 106.3420),
('VN-BL', 'Bạc Liêu', 'Bac Lieu', 'Miền Nam', 9.2940, 105.7215),
('VN-CM', 'Cà Mau', 'Ca Mau', 'Miền Nam', 9.1526, 105.1960),
('VN-KG', 'Kiên Giang', 'Kien Giang', 'Miền Nam', 10.0125, 105.0808),
('VN-AG', 'An Giang', 'An Giang', 'Miền Nam', 10.5216, 105.1258),
('VN-ĐT', 'Đồng Tháp', 'Dong Thap', 'Miền Nam', 10.4938, 105.6881),
('VN-VL', 'Vĩnh Long', 'Vinh Long', 'Miền Nam', 10.2395, 105.9572),
('VN-TV', 'Trà Vinh', 'Tra Vinh', 'Miền Nam', 9.8124, 106.2992),
('VN-ST', 'Sóc Trăng', 'Soc Trang', 'Miền Nam', 9.6025, 105.9739),
('VN-HG', 'Hậu Giang', 'Hau Giang', 'Miền Nam', 9.7579, 105.6412),
('VN-BT2', 'Bến Tre', 'Ben Tre', 'Miền Nam', 10.2433, 106.3757),
('VN-CT', 'Cần Thơ', 'Can Tho', 'Miền Nam', 10.0452, 105.7469)
ON CONFLICT (code) DO NOTHING;

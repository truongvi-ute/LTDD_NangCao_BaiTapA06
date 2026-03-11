import os
import json
import random
import requests
import concurrent.futures

PROVINCES = [
    {"code": "VN-HN", "name": "Hà Nội", "keyword": "hanoi,vietnam"},
    {"code": "VN-SG", "name": "TP.HCM", "keyword": "hochiminh,vietnam"},
    {"code": "VN-DN", "name": "Đà Nẵng", "keyword": "danang,vietnam"},
    {"code": "VN-HP", "name": "Hải Phòng", "keyword": "haiphong,vietnam"},
    {"code": "VN-CT", "name": "Cần Thơ", "keyword": "cantho,vietnam"},
    {"code": "VN-AG", "name": "An Giang", "keyword": "mekong,vietnam"},
    {"code": "VN-BV", "name": "Bà Rịa - Vũng Tàu", "keyword": "vungtau,beach"},
    {"code": "VN-BL", "name": "Bạc Liêu", "keyword": "mekong,vietnam"},
    {"code": "VN-BG", "name": "Bắc Giang", "keyword": "vietnam,countryside"},
    {"code": "VN-BK", "name": "Bắc Kạn", "keyword": "vietnam,lake"},
    {"code": "VN-BN", "name": "Bắc Ninh", "keyword": "vietnam,temple"},
    {"code": "VN-BT", "name": "Bến Tre", "keyword": "mekong,vietnam"},
    {"code": "VN-BD", "name": "Bình Dương", "keyword": "vietnam,city"},
    {"code": "VN-BP", "name": "Bình Phước", "keyword": "vietnam,forest"},
    {"code": "VN-BTH", "name": "Bình Thuận", "keyword": "muine,vietnam"},
    {"code": "VN-BDD", "name": "Bình Định", "keyword": "quynhon,vietnam"},
    {"code": "VN-CM", "name": "Cà Mau", "keyword": "mekong,vietnam"},
    {"code": "VN-CB", "name": "Cao Bằng", "keyword": "vietnam,waterfall"},
    {"code": "VN-GL", "name": "Gia Lai", "keyword": "vietnam,highland"},
    {"code": "VN-HG", "name": "Hà Giang", "keyword": "hagiang,vietnam"},
    {"code": "VN-HNA", "name": "Hà Nam", "keyword": "vietnam,pagoda"},
    {"code": "VN-HT", "name": "Hà Tĩnh", "keyword": "vietnam,landscape"},
    {"code": "VN-HD", "name": "Hải Dương", "keyword": "vietnam,city"},
    {"code": "VN-HA", "name": "Hậu Giang", "keyword": "mekong,vietnam"},
    {"code": "VN-HB", "name": "Hòa Bình", "keyword": "vietnam,mountain"},
    {"code": "VN-HY", "name": "Hưng Yên", "keyword": "vietnam,village"},
    {"code": "VN-KH", "name": "Khánh Hòa", "keyword": "nhatrang,vietnam"},
    {"code": "VN-KG", "name": "Kiên Giang", "keyword": "phuquoc,vietnam"},
    {"code": "VN-KT", "name": "Kon Tum", "keyword": "vietnam,highland"},
    {"code": "VN-LC", "name": "Lai Châu", "keyword": "vietnam,mountain"},
    {"code": "VN-LD", "name": "Lâm Đồng", "keyword": "dalat,vietnam"},
    {"code": "VN-LS", "name": "Lạng Sơn", "keyword": "vietnam,border"},
    {"code": "VN-LCA", "name": "Lào Cai", "keyword": "sapa,vietnam"},
    {"code": "VN-LA", "name": "Long An", "keyword": "mekong,vietnam"},
    {"code": "VN-ND", "name": "Nam Định", "keyword": "vietnam,church"},
    {"code": "VN-NA", "name": "Nghệ An", "keyword": "vietnam,beach"},
    {"code": "VN-NB", "name": "Ninh Bình", "keyword": "ninhbinh,vietnam"},
    {"code": "VN-NT", "name": "Ninh Thuận", "keyword": "vietnam,coast"},
    {"code": "VN-PT", "name": "Phú Thọ", "keyword": "vietnam,temple"},
    {"code": "VN-PY", "name": "Phú Yên", "keyword": "phuyen,vietnam"},
    {"code": "VN-QB", "name": "Quảng Bình", "keyword": "vietnam,cave"},
    {"code": "VN-QN", "name": "Quảng Nam", "keyword": "hoian,vietnam"},
    {"code": "VN-QNG", "name": "Quảng Ngãi", "keyword": "lyson,vietnam"},
    {"code": "VN-QNH", "name": "Quảng Ninh", "keyword": "halongbay,vietnam"},
    {"code": "VN-QT", "name": "Quảng Trị", "keyword": "vietnam,monument"},
    {"code": "VN-ST", "name": "Sóc Trăng", "keyword": "mekong,vietnam"},
    {"code": "VN-SL", "name": "Sơn La", "keyword": "mocchau,vietnam"},
    {"code": "VN-TN", "name": "Tây Ninh", "keyword": "vietnam,mountain"},
    {"code": "VN-TB", "name": "Thái Bình", "keyword": "vietnam,rice"},
    {"code": "VN-TY", "name": "Thái Nguyên", "keyword": "vietnam,tea"},
    {"code": "VN-TH", "name": "Thanh Hóa", "keyword": "vietnam,beach"},
    {"code": "VN-TTH", "name": "Thừa Thiên Huế", "keyword": "hue,vietnam"},
    {"code": "VN-TG", "name": "Tiền Giang", "keyword": "mekong,vietnam"},
    {"code": "VN-TV", "name": "Trà Vinh", "keyword": "mekong,vietnam"},
    {"code": "VN-TQ", "name": "Tuyên Quang", "keyword": "vietnam,lake"},
    {"code": "VN-VL", "name": "Vĩnh Long", "keyword": "mekong,vietnam"},
    {"code": "VN-VP", "name": "Vĩnh Phúc", "keyword": "tamdao,vietnam"},
    {"code": "VN-YB", "name": "Yên Bái", "keyword": "mucangchai,vietnam"},
    {"code": "VN-DB", "name": "Điện Biên", "keyword": "vietnam,monument"},
    {"code": "VN-DNO", "name": "Đắk Nông", "keyword": "vietnam,waterfall"},
    {"code": "VN-DL", "name": "Đắk Lắk", "keyword": "vietnam,coffee"},
    {"code": "VN-DT", "name": "Đồng Tháp", "keyword": "vietnam,lotus"},
    {"code": "VN-DNA", "name": "Đồng Nai", "keyword": "vietnam,nationalpark"}
]

CATEGORIES = ["LANDSCAPE", "PEOPLE", "FOOD", "ARCHITECTURE", "CULTURE", "NATURE", "URBAN", "EVENT", "OTHER"]

CAPTIONS = {
    "LANDSCAPE": ["Cảnh sắc hữu tình 🌅", "Góc nhìn tuyệt đẹp 🏞️", "Chốn bình yên 🌿"],
    "PEOPLE": ["Nụ cười thân thiện 👫", "Nhịp sống đời thường 👨‍🌾", "Con người giản dị 👩‍👧‍👦"],
    "FOOD": ["Đặc sản quá ngon! 🍜", "Hương vị truyền thống 🍲", "Một chút ẩm thực 🍱"],
    "ARCHITECTURE": ["Kiến trúc độc đáo 🏛️", "Dấu ấn thời gian ⛩️", "Công trình nổi bật 🏢"],
    "CULTURE": ["Giá trị truyền thống 🎭", "Nét đẹp văn hóa 🏮", "Tinh hoa dân tộc ✨"],
    "NATURE": ["Mẹ thiên nhiên 🌳", "Hòa mình cùng cỏ cây 🌿", "Chút bình yên giữa thiên nhiên 🦋"],
    "URBAN": ["Nhịp sống hối hả 🏙️", "Thành phố ánh sáng 🌃", "Góc phố quen thuộc 🚦"],
    "EVENT": ["Không khí sôi động 🎉", "Một ngày đặc biệt 🎊", "Hòa chung niềm vui 🎈"],
    "OTHER": ["Khoảnh khắc khó quên 📸", "Chút kỷ niệm 📌", "Rất đáng để trải nghiệm 💫"]
}

OUTPUT_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "uploads", "moments")
JSON_SEED_FILE = os.path.join(OUTPUT_DIR, "seed_data.json")

def ensure_dir(path):
    if not os.path.exists(path):
        os.makedirs(path)

def generate_caption(category_id, province_name):
    opts = CAPTIONS.get(category_id, CAPTIONS["OTHER"])
    base_cap = random.choice(opts)
    return f"{base_cap} tại {province_name}"

def download_image_task(task_args):
    province, cat_id, image_counter, seed = task_args
    filename = f"seed-{province['code']}-{cat_id.lower()}-{image_counter:03d}.jpg"
    filepath = os.path.join(OUTPUT_DIR, filename)

    if os.path.exists(filepath):
        print(f"  [Bỏ qua] Đã có ảnh: {filename}")
        return create_record(province, cat_id, filename)

    url = f"https://loremflickr.com/800/800/{province['keyword']},{cat_id.lower()}/all?lock={seed}"
    
    try:
        response = requests.get(url, timeout=15, stream=True)
        if response.status_code == 200:
            with open(filepath, 'wb') as f:
                for chunk in response.iter_content(1024):
                    f.write(chunk)
            print(f"  => THÀNH CÔNG! Đã lưu {filename}")
            return create_record(province, cat_id, filename)
    except Exception as e:
        print(f"  [!] Lỗi khi tải {filename}: {e}")
    return None

PROVINCE_COORDS = {
    "VN-HN": (21.0285, 105.8542), "VN-SG": (10.8231, 106.6297), "VN-DN": (16.0544, 108.2022),
    "VN-HP": (20.8449, 106.6881), "VN-CT": (10.0452, 105.7469), "VN-AG": (10.5369, 105.1183),
    "VN-BV": (10.4938, 107.1661), "VN-BL": (9.2941, 105.7278), "VN-BG": (21.2731, 106.1946),
    "VN-BK": (22.1471, 105.8348), "VN-BN": (21.1861, 106.0763), "VN-BT": (10.2435, 106.3752),
    "VN-BD": (11.0256, 106.6575), "VN-BP": (11.7516, 106.9189), "VN-BTH": (10.9333, 108.1000),
    "VN-BDD": (13.7829, 109.2197), "VN-CM": (9.1769, 105.1500), "VN-CB": (22.6667, 106.2500),
    "VN-GL": (13.9833, 108.0000), "VN-HG": (22.8167, 104.9833), "VN-HNA": (20.5453, 105.9122),
    "VN-HT": (18.3333, 105.9000), "VN-HD": (20.9333, 106.3167), "VN-HA": (9.7833, 105.4667),
    "VN-HB": (20.8167, 105.3333), "VN-HY": (20.6464, 106.0511), "VN-KH": (12.2500, 109.1833),
    "VN-KG": (10.0167, 105.0833), "VN-KT": (14.3500, 108.0000), "VN-LC": (22.4000, 103.4667),
    "VN-LD": (11.9465, 108.4419), "VN-LS": (21.8333, 106.7667), "VN-LCA": (22.3333, 103.8333),
    "VN-LA": (10.5333, 106.4000), "VN-ND": (20.4333, 106.1667), "VN-NA": (18.6667, 105.6667),
    "VN-NB": (20.2500, 105.9667), "VN-NT": (11.5667, 108.9833), "VN-PT": (21.3167, 105.2167),
    "VN-PY": (13.0833, 109.3000), "VN-QB": (17.4833, 106.6000), "VN-QN": (15.5833, 108.0000),
    "VN-QNG": (15.1167, 108.8000), "VN-QNH": (21.0000, 107.3333), "VN-QT": (16.7500, 107.1833),
    "VN-ST": (9.6000, 105.9667), "VN-SL": (21.3333, 103.9000), "VN-TN": (11.3000, 106.1000),
    "VN-TB": (20.4500, 106.3333), "VN-TY": (21.5833, 105.8333), "VN-TH": (19.8000, 105.7667),
    "VN-TTH": (16.4667, 107.6000), "VN-TG": (10.3500, 106.3500), "VN-TV": (9.9333, 106.3333),
    "VN-TQ": (22.0667, 105.2667), "VN-VL": (10.2500, 105.9667), "VN-VP": (21.3167, 105.6000),
    "VN-YB": (21.7167, 104.8667), "VN-DB": (21.3833, 103.0167), "VN-DNO": (12.2667, 107.7167),
    "VN-DL": (12.6667, 108.0500), "VN-DT": (10.4667, 105.6333), "VN-DNA": (10.9333, 106.8167)
}

def create_record(province, cat_id, filename):
    p_code = province["code"]
    base_lat, base_lng = PROVINCE_COORDS.get(p_code, (16.0, 106.0))
    # Cộng trừ ngẫu nhiên nhỏ để các điểm trong cùng tỉnh không trùng y xì
    lat_val = base_lat + (random.random() - 0.5) * 0.1
    lng_val = base_lng + (random.random() - 0.5) * 0.1
    
    return {
        "filename": filename,
        "category": cat_id,
        "provinceCode": province["code"],
        "provinceName": province["name"],
        "addressName": f"Một góc nhỏ ở {province['name']}",
        "caption": generate_caption(cat_id, province["name"]),
        "latitude": lat_val,
        "longitude": lng_val
    }

def main():
    print(f"[*] Bắt đầu script tải ảnh Data Seed (nhanh) cho 63 tỉnh thành.")
    ensure_dir(OUTPUT_DIR)
    
    tasks = []
    image_counter = 1
    global_seed = random.randint(1000, 9999)
    
    # Generate tasks
    for index, province in enumerate(PROVINCES):
        num_images = random.randint(2, 3) 
        if province["code"] in ["VN-HN", "VN-SG", "VN-DN", "VN-QNH", "VN-LD", "VN-KH", "VN-TTH", "VN-QN"]:
            num_images = random.randint(4, 5)
            
        cats = random.sample(CATEGORIES, num_images)
        for cat in cats:
            tasks.append((province, cat, image_counter, global_seed + image_counter))
            image_counter += 1

    print(f"[*] Chuẩn bị tải {len(tasks)} ảnh...")

    seed_records = []
    
    # Run with 15 parallel threads for fast downloading
    with concurrent.futures.ThreadPoolExecutor(max_workers=15) as executor:
        results = executor.map(download_image_task, tasks)
        for r in results:
            if r:
                seed_records.append(r)

    print(f"\n[*] Đã tải thành công {len(seed_records)} ảnh.")
    print(f"[*] Đang lưu file dữ liệu mẫu vào {JSON_SEED_FILE}")
    
    with open(JSON_SEED_FILE, 'w', encoding='utf-8') as f:
        json.dump(seed_records, f, ensure_ascii=False, indent=2)
        
    print("[*] Hoàn tất script!")

if __name__ == "__main__":
    main()

# 🔍 搜尋與排序功能完整實作報告

## 📋 完成概況

**完成日期**: 2025-11-08  
**功能**: 完整的搜尋與排序系統（仿 Booking.com）  
**狀態**: ✅ 已完成並編譯成功

---

## ✅ 實作內容總覽

### 1. 資料模型擴充 ✅

**檔案**: `Accommodation.java`

**新增欄位**:
- `rating` (BigDecimal) - 評分 (0-5)
- `reviewCount` (Integer) - 評論數量
- `bookingCount` (Integer) - 訂房次數（用於熱門度計算）
- `distanceFromCenter` (BigDecimal) - 距離市中心（公里）

**用途**:
```java
@Column(name = "rating", precision = 3, scale = 2)
private BigDecimal rating; // 4.5 星評分

@Column(name = "review_count")
private Integer reviewCount = 0; // 128 則評論

@Column(name = "booking_count")
private Integer bookingCount = 0; // 256 次訂房

@Column(name = "distance_from_center", precision = 5, scale = 2)
private BigDecimal distanceFromCenter; // 1.2 公里
```

---

### 2. 排序功能實作 ✅

**檔案**: `BookingService.java`

**支援的排序方式**:

| 排序方式 | API 參數 | 說明 | 演算法 |
|---------|----------|------|--------|
| 價格低到高 | `price_asc`, `price_low` | 最便宜的住宿排前面 | priceA.compareTo(priceB) |
| 價格高到低 | `price_desc`, `price_high` | 最貴的住宿排前面 | priceB.compareTo(priceA) |
| 評分最高 | `rating`, `rating_desc` | 評分高的排前面 | 評分相同時按評論數 |
| 最受歡迎 | `popularity`, `recommended` | 綜合熱門度排序 | 訂房次數×0.7 + 評分×評論數×0.3 |
| 距離最近 | `distance`, `distance_asc` | 離市中心最近 | distance.compareTo() |
| 名稱 A-Z | `name_asc`, `name_a_z` | 按名稱字母順序 | nameA.compareToIgnoreCase(nameB) |
| 名稱 Z-A | `name_desc`, `name_z_a` | 按名稱反向順序 | nameB.compareToIgnoreCase(nameA) |

**核心方法**:
```java
private List<Accommodation> sortAccommodations(List<Accommodation> accommodations, String sortBy) {
    if (sortBy == null || sortBy.isBlank()) {
        return accommodations; // 不排序
    }

    return switch (sortBy.toLowerCase()) {
        case "price_asc", "price_low" -> /* 價格低到高 */
        case "price_desc", "price_high" -> /* 價格高到低 */
        case "rating", "rating_desc" -> /* 評分最高 */
        case "popularity", "recommended" -> /* 最受歡迎 */
        case "distance", "distance_asc" -> /* 距離最近 */
        case "name_asc", "name_a_z" -> /* 名稱 A-Z */
        case "name_desc", "name_z_a" -> /* 名稱 Z-A */
        default -> accommodations; // 未知方式，返回原始順序
    };
}
```

---

### 3. API 端點更新 ✅

**檔案**: `AccommodationController.java`

#### 3.1 取得所有住宿（支援排序）

**端點**: `GET /api/accommodations`

**參數**:
- `sortBy` (可選) - 排序方式

**範例**:
```
GET /api/accommodations?sortBy=price_asc
GET /api/accommodations?sortBy=rating
GET /api/accommodations?sortBy=popularity
```

#### 3.2 搜尋住宿（支援排序）

**端點**: `GET /api/accommodations/search`

**參數**:
- `location` (必填) - 地點關鍵字
- `sortBy` (可選) - 排序方式

**範例**:
```
GET /api/accommodations/search?location=台北&sortBy=price_asc
GET /api/accommodations/search?location=高雄&sortBy=rating
```

---

### 4. 前端界面整合 ✅

**檔案**: `index.html`

#### 4.1 排序選擇器

```html
<select id="sortBy" class="form-select" onchange="applySorting()">
    <option value="">預設順序</option>
    <option value="price_asc">💰 價格：低到高</option>
    <option value="price_desc">💰 價格：高到低</option>
    <option value="rating">⭐ 評分最高</option>
    <option value="popularity">🔥 最受歡迎</option>
    <option value="distance">📍 距離最近</option>
    <option value="name_asc">🔤 名稱：A-Z</option>
    <option value="name_desc">🔤 名稱：Z-A</option>
</select>
```

#### 4.2 住宿卡片顯示增強

**新增顯示項目**:
- ⭐ 評分標籤（黃色徽章）
- 📝 評論數量
- 🚶 距離市中心
- 🔥 熱門標籤（訂房次數 > 100）

**範例**:
```html
<div class="card">
    <h5>台北商旅</h5>
    <div>
        <span class="badge bg-warning">⭐ 4.5</span>
        <small>(128 則評論)</small>
    </div>
    <p>
        📍 台北<br>
        💰 NT$ 2200 / 晚<br>
        🚶 距離市中心: 1.2 公里<br>
        <span class="badge bg-danger">🔥 熱門</span>
    </p>
</div>
```

#### 4.3 JavaScript 函數更新

**新增/修改的函數**:

```javascript
// 載入所有住宿（支援排序）
function loadAllAccommodations() {
    const sortBy = document.getElementById('sortBy')?.value || '';
    const url = sortBy ? `/api/accommodations?sortBy=${sortBy}` : '/api/accommodations';
    fetch(url).then(r => r.json()).then(displayAccommodations);
}

// 搜尋住宿（支援排序）
function searchByLocation() {
    const location = document.getElementById('locationSearch').value.trim();
    const sortBy = document.getElementById('sortBy')?.value || '';
    let url = '/api/accommodations/search?location=' + encodeURIComponent(location);
    if (sortBy) url += '&sortBy=' + sortBy;
    fetch(url).then(r => r.json()).then(displayAccommodations);
}

// 應用排序
function applySorting() {
    const locationSearch = document.getElementById('locationSearch').value.trim();
    if (locationSearch) {
        searchByLocation(); // 重新搜尋並排序
    } else {
        loadAllAccommodations(); // 重新載入並排序
    }
}
```

---

### 5. 測試資料更新 ✅

**檔案**: `data.sql`

**新增 8 個住宿**（包含完整資訊）:

| 住宿名稱 | 地點 | 價格 | 評分 | 評論數 | 訂房次數 | 距離 |
|---------|------|------|------|--------|---------|------|
| 台北商旅 | 台北 | 2200 | 4.5 | 128 | 256 | 1.2km |
| 高雄港景飯店 | 高雄 | 1800 | 4.8 | 95 | 180 | 3.5km |
| 台中精品旅館 | 台中 | 2500 | 4.2 | 76 | 142 | 0.8km |
| 花蓮民宿 | 花蓮 | 1500 | 4.9 | 203 | 398 | 5.2km |
| 台北經濟旅館 | 台北 | 1200 | 3.8 | 45 | 89 | 2.5km |
| 台南古蹟民宿 | 台南 | 1600 | 4.6 | 112 | 198 | 1.5km |
| 墾丁海景度假村 | 墾丁 | 3800 | 4.7 | 156 | 287 | 0.3km |
| 宜蘭溫泉飯店 | 宜蘭 | 2800 | 4.4 | 89 | 167 | 4.1km |

**配套房型**: 16 種房型，每個住宿 1-2 種房型

---

## 🎯 排序演算法詳解

### 1. 價格排序

**簡單比較**:
```java
case "price_asc" -> accommodations.stream()
    .sorted((a, b) -> {
        BigDecimal priceA = a.getPricePerNight() != null ? a.getPricePerNight() : BigDecimal.ZERO;
        BigDecimal priceB = b.getPricePerNight() != null ? b.getPricePerNight() : BigDecimal.ZERO;
        return priceA.compareTo(priceB); // 低到高
    })
    .toList();
```

**結果**:
1. 台北經濟旅館 - NT$ 1200
2. 花蓮民宿 - NT$ 1500
3. 台南古蹟民宿 - NT$ 1600
4. ...

---

### 2. 評分排序

**雙重排序** (評分 → 評論數):
```java
case "rating" -> accommodations.stream()
    .sorted((a, b) -> {
        BigDecimal ratingA = a.getRating() != null ? a.getRating() : BigDecimal.ZERO;
        BigDecimal ratingB = b.getRating() != null ? b.getRating() : BigDecimal.ZERO;
        int ratingCompare = ratingB.compareTo(ratingA); // 評分高到低
        if (ratingCompare != 0) return ratingCompare;
        
        // 評分相同時，按評論數量排序
        Integer reviewA = a.getReviewCount() != null ? a.getReviewCount() : 0;
        Integer reviewB = b.getReviewCount() != null ? b.getReviewCount() : 0;
        return reviewB.compareTo(reviewA);
    })
    .toList();
```

**結果**:
1. 花蓮民宿 - ⭐ 4.9 (203 則評論)
2. 高雄港景飯店 - ⭐ 4.8 (95 則評論)
3. 墾丁海景度假村 - ⭐ 4.7 (156 則評論)
4. ...

---

### 3. 熱門度排序（推薦）

**綜合評分演算法**:
```java
case "popularity" -> accommodations.stream()
    .sorted((a, b) -> {
        // 綜合評分 = 訂房次數 × 0.7 + (評分 × 評論數) × 0.3
        Integer bookingA = a.getBookingCount() != null ? a.getBookingCount() : 0;
        Integer bookingB = b.getBookingCount() != null ? b.getBookingCount() : 0;
        
        BigDecimal ratingA = a.getRating() != null ? a.getRating() : BigDecimal.ZERO;
        BigDecimal ratingB = b.getRating() != null ? b.getRating() : BigDecimal.ZERO;
        Integer reviewA = a.getReviewCount() != null ? a.getReviewCount() : 0;
        Integer reviewB = b.getReviewCount() != null ? b.getReviewCount() : 0;
        
        double scoreA = bookingA * 0.7 + ratingA.doubleValue() * reviewA * 0.3;
        double scoreB = bookingB * 0.7 + ratingB.doubleValue() * reviewB * 0.3;
        
        return Double.compare(scoreB, scoreA); // 高到低
    })
    .toList();
```

**計算範例**:
- **花蓮民宿**: 398 × 0.7 + (4.9 × 203) × 0.3 = 278.6 + 298.47 = **577.07**
- **墾丁海景度假村**: 287 × 0.7 + (4.7 × 156) × 0.3 = 200.9 + 219.96 = **420.86**
- **台北商旅**: 256 × 0.7 + (4.5 × 128) × 0.3 = 179.2 + 172.8 = **352.0**

**結果順序**: 花蓮民宿 > 墾丁海景度假村 > 台北商旅

---

### 4. 距離排序

**近到遠排序**:
```java
case "distance" -> accommodations.stream()
    .sorted((a, b) -> {
        BigDecimal distA = a.getDistanceFromCenter() != null ? 
            a.getDistanceFromCenter() : BigDecimal.valueOf(999);
        BigDecimal distB = b.getDistanceFromCenter() != null ? 
            b.getDistanceFromCenter() : BigDecimal.valueOf(999);
        return distA.compareTo(distB); // 近到遠
    })
    .toList();
```

**結果**:
1. 墾丁海景度假村 - 0.3 km
2. 台中精品旅館 - 0.8 km
3. 台北商旅 - 1.2 km
4. ...

---

## 🧪 測試案例

### 測試 1: 價格排序

**請求**:
```
GET /api/accommodations?sortBy=price_asc
```

**預期結果** (前3名):
1. 台北經濟旅館 - NT$ 1200
2. 花蓮民宿 - NT$ 1500
3. 台南古蹟民宿 - NT$ 1600

---

### 測試 2: 評分排序

**請求**:
```
GET /api/accommodations?sortBy=rating
```

**預期結果** (前3名):
1. 花蓮民宿 - ⭐ 4.9
2. 高雄港景飯店 - ⭐ 4.8
3. 墾丁海景度假村 - ⭐ 4.7

---

### 測試 3: 熱門度排序

**請求**:
```
GET /api/accommodations?sortBy=popularity
```

**預期結果** (前3名):
1. 花蓮民宿 (綜合分數: 577.07)
2. 墾丁海景度假村 (綜合分數: 420.86)
3. 台北商旅 (綜合分數: 352.0)

---

### 測試 4: 搜尋 + 排序

**請求**:
```
GET /api/accommodations/search?location=台北&sortBy=price_asc
```

**預期結果**:
1. 台北經濟旅館 - NT$ 1200
2. 台北商旅 - NT$ 2200

---

### 測試 5: 距離排序

**請求**:
```
GET /api/accommodations?sortBy=distance
```

**預期結果** (前3名):
1. 墾丁海景度假村 - 0.3 km
2. 台中精品旅館 - 0.8 km
3. 台北商旅 - 1.2 km

---

## 🎨 前端使用指南

### 使用方式 1: 直接排序

1. 訪問首頁 `http://localhost:8080/`
2. 在右上方選擇排序方式
3. 住宿列表自動重新排序

### 使用方式 2: 搜尋後排序

1. 輸入地點（例如：台北）
2. 點擊「搜尋」
3. 選擇排序方式
4. 搜尋結果自動重新排序

### 使用方式 3: API 直接調用

**使用 Swagger UI**:
```
http://localhost:8080/swagger-ui.html
```

**或使用 curl**:
```bash
# 價格低到高
curl "http://localhost:8080/api/accommodations?sortBy=price_asc"

# 評分最高
curl "http://localhost:8080/api/accommodations?sortBy=rating"

# 搜尋台北 + 價格排序
curl "http://localhost:8080/api/accommodations/search?location=台北&sortBy=price_asc"
```

---

## 📊 功能對照表

### 與 Booking.com 的功能對照

| 功能 | Booking.com | 本系統 | 狀態 |
|------|-------------|--------|------|
| 價格排序 | ✅ | ✅ | 完成 |
| 評分排序 | ✅ | ✅ | 完成 |
| 推薦排序 | ✅ | ✅ | 完成 |
| 距離排序 | ✅ | ✅ | 完成 |
| 名稱排序 | ❌ | ✅ | 額外功能 |
| 設施篩選 | ✅ | ⏳ | 未來功能 |
| 星級篩選 | ✅ | ⏳ | 未來功能 |
| 價格範圍 | ✅ | ⏳ | 未來功能 |

---

## 🚀 未來擴展建議

### 1. 進階篩選功能

**價格範圍篩選**:
```java
public List<Accommodation> filterByPriceRange(BigDecimal min, BigDecimal max) {
    return accommodations.stream()
        .filter(a -> {
            BigDecimal price = a.getPricePerNight();
            return price.compareTo(min) >= 0 && price.compareTo(max) <= 0;
        })
        .toList();
}
```

**設施篩選**:
```java
public List<Accommodation> filterByAmenities(List<String> requiredAmenities) {
    return accommodations.stream()
        .filter(a -> {
            String amenities = a.getAmenities();
            return requiredAmenities.stream()
                .allMatch(req -> amenities.contains(req));
        })
        .toList();
}
```

### 2. 複合排序

**價格 + 評分**:
```java
.sorted(Comparator
    .comparing(Accommodation::getPricePerNight)
    .thenComparing(Accommodation::getRating, Comparator.reverseOrder())
)
```

### 3. 分頁功能

```java
public Page<Accommodation> getAccommodations(Pageable pageable) {
    return accommodationRepository.findAll(pageable);
}
```

### 4. 快取優化

```java
@Cacheable(value = "accommodations", key = "#sortBy")
public List<Accommodation> getAllAccommodations(String sortBy) {
    // ...
}
```

---

## 📝 API 文件總結

### GET /api/accommodations

**描述**: 取得所有住宿並排序

**參數**:
| 參數 | 類型 | 必填 | 說明 | 範例 |
|------|------|------|------|------|
| sortBy | String | 否 | 排序方式 | price_asc |

**回應**:
```json
[
  {
    "id": 5,
    "name": "台北經濟旅館",
    "location": "台北",
    "pricePerNight": 1200.00,
    "rating": 3.8,
    "reviewCount": 45,
    "bookingCount": 89,
    "distanceFromCenter": 2.5
  }
]
```

### GET /api/accommodations/search

**描述**: 搜尋住宿並排序

**參數**:
| 參數 | 類型 | 必填 | 說明 | 範例 |
|------|------|------|------|------|
| location | String | 是 | 地點關鍵字 | 台北 |
| sortBy | String | 否 | 排序方式 | price_asc |

**回應**: 同上

---

## ✨ 總結

### 完成項目 ✅

- ✅ Accommodation 模型擴充（4 個新欄位）
- ✅ 排序演算法實作（7 種排序方式）
- ✅ API 端點更新（2 個端點支援排序）
- ✅ 前端界面整合（排序選擇器 + 卡片增強）
- ✅ 測試資料準備（8 個住宿 + 完整資訊）
- ✅ JavaScript 函數更新（支援動態排序）
- ✅ 編譯測試通過

### 代碼統計

| 類型 | 檔案數 | 新增行數 | 修改行數 |
|------|--------|---------|---------|
| Model | 1 | +15 | ~5 |
| Service | 1 | +110 | ~10 |
| Controller | 1 | +6 | ~15 |
| HTML | 1 | +35 | ~20 |
| JavaScript | 1 | +25 | ~15 |
| SQL | 1 | +16 | ~12 |
| **總計** | **6** | **~207** | **~77** |

### 支援的排序方式

1. 💰 **價格排序** - 低到高、高到低
2. ⭐ **評分排序** - 最高評分優先，相同評分看評論數
3. 🔥 **熱門度排序** - 綜合訂房次數和評分
4. 📍 **距離排序** - 離市中心由近到遠
5. 🔤 **名稱排序** - A-Z、Z-A

### 測試狀態

- ✅ 編譯成功
- ⏳ 功能測試待執行
- ⏳ 效能測試待執行

### 使用方式

1. **重啟應用程式**
2. **訪問首頁** `http://localhost:8080/`
3. **選擇排序方式** 從下拉選單
4. **查看結果** 住宿列表自動重新排序

---

**文件建立日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 完整實作完成


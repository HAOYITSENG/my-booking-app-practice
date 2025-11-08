# 🔍 首頁搜尋功能升級報告 - Booking.com 風格

## 📋 更新概況

**完成日期**: 2025-11-09  
**功能**: 首頁搜尋升級為 Booking.com 風格統一搜尋列  
**狀態**: ✅ 已完成並編譯成功

---

## ✨ 主要改進

### 從分散式搜尋升級到統一搜尋

#### 升級前（舊版）
```
分散的搜尋區域：
- ❌ 地點搜尋（單獨的卡片）
- ❌ 日期查詢（單獨的卡片）
- ❌ 沒有人數選擇
- ❌ 只能搜尋地點
- ❌ UI 分散不直觀
```

#### 升級後（新版）
```
Booking.com 風格統一搜尋：
✅ 地點或飯店名稱（統一欄位）
✅ 入住/退房日期（預設明後天）
✅ 人數選擇（成人數量）
✅ 所有條件整合在一行
✅ 即時搜尋結果提示
✅ 美觀的漸變背景
✅ 支援複合搜尋
```

---

## 📝 詳細變更

### 1. 前端 UI 改進

#### 1.1 新的搜尋區域設計

```html
<div class="search-hero mb-4">
    <div class="card shadow-lg border-0">
        <div class="card-body p-4">
            <h2 class="text-center mb-4">🏨 尋找您的理想住宿</h2>
            
            <div class="row g-3 align-items-end">
                <!-- 地點/飯店名稱 -->
                <div class="col-md-3">
                    <label class="form-label fw-bold">
                        <i class="bi bi-geo-alt-fill"></i> 地點或飯店名稱
                    </label>
                    <input type="text" 
                           class="form-control form-control-lg" 
                           id="searchQuery" 
                           placeholder="例：台北、日安旅館">
                    <small class="text-muted">輸入城市、地區或飯店名稱</small>
                </div>

                <!-- 入住日期 -->
                <div class="col-md-2">
                    <label class="form-label fw-bold">
                        <i class="bi bi-calendar-check"></i> 入住日期
                    </label>
                    <input type="date" class="form-control form-control-lg" id="checkIn">
                </div>

                <!-- 退房日期 -->
                <div class="col-md-2">
                    <label class="form-label fw-bold">
                        <i class="bi bi-calendar-x"></i> 退房日期
                    </label>
                    <input type="date" class="form-control form-control-lg" id="checkOut">
                </div>

                <!-- 人數選擇 -->
                <div class="col-md-2">
                    <label class="form-label fw-bold">
                        <i class="bi bi-people-fill"></i> 入住人數
                    </label>
                    <div class="input-group input-group-lg">
                        <button class="btn btn-outline-secondary" 
                                type="button" onclick="changeGuests(-1)">-</button>
                        <input type="number" class="form-control text-center" 
                               id="guests" value="2" min="1" max="10" readonly>
                        <button class="btn btn-outline-secondary" 
                                type="button" onclick="changeGuests(1)">+</button>
                    </div>
                    <small class="text-muted">2 位成人</small>
                </div>

                <!-- 搜尋按鈕 -->
                <div class="col-md-3">
                    <button class="btn btn-primary btn-lg w-100" 
                            onclick="performSearch()">
                        🔍 搜尋住宿
                    </button>
                    <button class="btn btn-outline-secondary btn-sm w-100 mt-2" 
                            onclick="clearSearch()">
                        清除條件
                    </button>
                </div>
            </div>

            <!-- 搜尋結果提示 -->
            <div id="searchInfo" class="mt-3 text-center text-muted" style="display:none;">
                <small></small>
            </div>
        </div>
    </div>
</div>
```

**特點**:
- ✅ 漸變紫色背景
- ✅ 大尺寸輸入框（lg）
- ✅ 圖示標示
- ✅ 提示文字
- ✅ 響應式設計

---

#### 1.2 樣式設計

```css
.search-hero {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 15px;
    padding: 10px;
}

.search-hero .card {
    border-radius: 10px;
}

.search-hero h2 {
    color: #333;
    font-weight: 700;
}

.search-hero .form-label {
    color: #333;
    font-size: 14px;
    margin-bottom: 5px;
}

.search-hero .form-control:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 0.25rem rgba(102, 126, 234, 0.25);
}

#searchInfo {
    padding: 10px;
    background: #f8f9fa;
    border-radius: 5px;
}
```

---

### 2. JavaScript 功能升級

#### 2.1 初始化搜尋日期

```javascript
function initSearchDates() {
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dayAfter = new Date(today);
    dayAfter.setDate(dayAfter.getDate() + 2);

    const checkInInput = document.getElementById('checkIn');
    const checkOutInput = document.getElementById('checkOut');
    
    checkInInput.min = today.toISOString().split('T')[0];
    checkOutInput.min = tomorrow.toISOString().split('T')[0];
    
    // 預設值：明天入住、後天退房
    checkInInput.value = tomorrow.toISOString().split('T')[0];
    checkOutInput.value = dayAfter.toISOString().split('T')[0];
}
```

**功能**:
- ✅ 鎖定過去日期
- ✅ 預設明天入住
- ✅ 預設後天退房

---

#### 2.2 入住日期變更處理

```javascript
function handleCheckInChange() {
    const checkIn = document.getElementById('checkIn').value;
    const checkOut = document.getElementById('checkOut').value;
    
    if (checkIn) {
        const checkInDate = new Date(checkIn);
        const minCheckOut = new Date(checkInDate);
        minCheckOut.setDate(minCheckOut.getDate() + 1);
        
        document.getElementById('checkOut').min = minCheckOut.toISOString().split('T')[0];
        
        // 如果退房日期早於或等於入住日期，自動調整
        if (!checkOut || new Date(checkOut) <= checkInDate) {
            document.getElementById('checkOut').value = minCheckOut.toISOString().split('T')[0];
        }
    }
}
```

**功能**:
- ✅ 動態調整退房日期下限
- ✅ 自動修正不合理的日期
- ✅ 防止日期邏輯錯誤

---

#### 2.3 人數變更

```javascript
function changeGuests(delta) {
    const input = document.getElementById('guests');
    const current = parseInt(input.value);
    const newValue = Math.max(1, Math.min(10, current + delta));
    input.value = newValue;
    document.getElementById('guestsText').textContent = `${newValue} 位成人`;
}
```

**功能**:
- ✅ 最少 1 人，最多 10 人
- ✅ +/- 按鈕控制
- ✅ 即時顯示人數文字

---

#### 2.4 統一搜尋功能（核心）

```javascript
function performSearch() {
    const query = document.getElementById('searchQuery').value.trim();
    const checkIn = document.getElementById('checkIn').value;
    const checkOut = document.getElementById('checkOut').value;
    const guests = document.getElementById('guests').value;
    const sortBy = document.getElementById('sortBy').value;

    // 建立搜尋條件
    let searchParams = new URLSearchParams();
    
    if (query) {
        searchParams.append('query', query);
    }
    
    if (checkIn && checkOut) {
        if (checkIn >= checkOut) {
            showAlert('入住日期必須早於退房日期', 'warning');
            return;
        }
        searchParams.append('checkIn', checkIn);
        searchParams.append('checkOut', checkOut);
    }
    
    if (guests) {
        searchParams.append('guests', guests);
    }
    
    if (sortBy) {
        searchParams.append('sortBy', sortBy);
    }

    // 決定使用哪個 API
    let url = '/api/accommodations';
    
    if (query && (checkIn && checkOut)) {
        // 複合搜尋：關鍵字 + 日期
        url = `/api/accommodations/search?${searchParams.toString()}`;
    } else if (query) {
        // 只有關鍵字
        url = `/api/accommodations/search?query=${encodeURIComponent(query)}`;
        if (sortBy) url += `&sortBy=${sortBy}`;
    } else if (checkIn && checkOut) {
        // 只有日期
        url = `/api/accommodations/available?checkIn=${checkIn}&checkOut=${checkOut}`;
        if (sortBy) url += `&sortBy=${sortBy}`;
    } else if (sortBy) {
        // 只有排序
        url = `/api/accommodations?sortBy=${sortBy}`;
    }

    // 顯示搜尋資訊
    updateSearchInfo(query, checkIn, checkOut, guests);

    // 執行搜尋
    fetch(url)
        .then(r => r.json())
        .then(data => {
            displayAccommodations(data);
            updateResultsTitle(query, checkIn, checkOut);
        })
        .catch(() => showAlert('搜尋失敗，請稍後再試', 'danger'));
}
```

**功能**:
- ✅ 智能判斷搜尋類型
- ✅ 支援複合條件
- ✅ 參數編碼安全
- ✅ 錯誤處理

---

#### 2.5 搜尋資訊顯示

```javascript
function updateSearchInfo(query, checkIn, checkOut, guests) {
    const searchInfo = document.getElementById('searchInfo');
    const parts = [];
    
    if (query) parts.push(`關鍵字: "${query}"`);
    if (checkIn && checkOut) {
        const nights = calculateDays(checkIn, checkOut);
        parts.push(`${checkIn} - ${checkOut} (${nights}晚)`);
    }
    if (guests) parts.push(`${guests}位成人`);
    
    if (parts.length > 0) {
        searchInfo.querySelector('small').textContent = `搜尋條件: ${parts.join(' | ')}`;
        searchInfo.style.display = 'block';
    } else {
        searchInfo.style.display = 'none';
    }
}
```

**顯示範例**:
```
搜尋條件: 關鍵字: "台北" | 2025-11-10 - 2025-11-12 (2晚) | 2位成人
```

---

#### 2.6 結果標題更新

```javascript
function updateResultsTitle(query, checkIn, checkOut) {
    const title = document.getElementById('resultsTitle');
    
    if (query && (checkIn && checkOut)) {
        title.textContent = `"${query}" 的可用住宿`;
    } else if (query) {
        title.textContent = `"${query}" 的搜尋結果`;
    } else if (checkIn && checkOut) {
        title.textContent = `可用住宿`;
    } else {
        title.textContent = `所有住宿`;
    }
}
```

---

#### 2.7 清除搜尋

```javascript
function clearSearch() {
    document.getElementById('searchQuery').value = '';
    document.getElementById('guests').value = '2';
    document.getElementById('guestsText').textContent = '2 位成人';
    document.getElementById('sortBy').value = '';
    
    initSearchDates(); // 重置日期為預設值
    
    loadAllAccommodations();
    
    document.getElementById('searchInfo').style.display = 'none';
    document.getElementById('resultsTitle').textContent = '所有住宿';
}
```

---

#### 2.8 結果顯示優化

```javascript
function displayAccommodations(list) {
    const c = document.getElementById('accommodations');
    const noResults = document.getElementById('noResults');
    const resultsCount = document.getElementById('resultsCount');
    
    if (!list || list.length === 0) {
        c.innerHTML = '';
        noResults.style.display = 'block';
        resultsCount.textContent = '找不到符合條件的住宿';
        return;
    }

    noResults.style.display = 'none';
    resultsCount.textContent = `找到 ${list.length} 間住宿`;
    
    // 渲染住宿卡片...
}
```

**顯示**:
```
找到 8 間住宿
```

---

### 3. 後端 API 升級

#### 3.1 統一搜尋 API

**AccommodationController.java**:

```java
@GetMapping("/search")
public List<Accommodation> searchAccommodations(
    @RequestParam(required = false) String query,           // 統一關鍵字
    @RequestParam(required = false) String location,        // 向下兼容
    @RequestParam(required = false) LocalDate checkIn,      // 入住日期
    @RequestParam(required = false) LocalDate checkOut,     // 退房日期
    @RequestParam(required = false) Integer guests,         // 人數（預留）
    @RequestParam(required = false) String sortBy           // 排序
) {
    // 統一使用 query 參數，但保留 location 以向下兼容
    String searchKeyword = query != null ? query : location;
    
    // 如果有日期，執行可用性搜尋
    if (checkIn != null && checkOut != null) {
        List<Accommodation> available = bookingService.getAvailableAccommodations(checkIn, checkOut);
        
        // 如果還有關鍵字，進一步篩選
        if (searchKeyword != null && !searchKeyword.isBlank()) {
            String keyword = searchKeyword.toLowerCase().trim();
            available = available.stream()
                .filter(acc -> 
                    acc.getLocation().toLowerCase().contains(keyword) ||
                    acc.getName().toLowerCase().contains(keyword)
                )
                .collect(Collectors.toList());
        }
        
        return bookingService.sortAccommodations(available, sortBy);
    }
    
    // 只有關鍵字的情況
    if (searchKeyword != null && !searchKeyword.isBlank()) {
        return bookingService.searchByLocationOrName(searchKeyword, sortBy);
    }
    
    // 沒有任何條件，返回所有住宿
    return bookingService.getAllAccommodations(sortBy);
}
```

**功能**:
- ✅ 支援關鍵字搜尋（地點或名稱）
- ✅ 支援日期篩選
- ✅ 支援複合條件
- ✅ 向下兼容舊 API
- ✅ 預留人數參數

---

#### 3.2 新的 Service 方法

**BookingService.java**:

```java
public List<Accommodation> searchByLocationOrName(String keyword, String sortBy) {
    List<Accommodation> accommodations;
    if (keyword == null || keyword.isBlank()) {
        accommodations = accommodationRepo.findAll();
    } else {
        String searchKeyword = keyword.trim().toLowerCase();
        // 搜尋地點或名稱包含關鍵字的住宿
        accommodations = accommodationRepo.findAll().stream()
                .filter(acc -> 
                    acc.getLocation().toLowerCase().contains(searchKeyword) ||
                    acc.getName().toLowerCase().contains(searchKeyword)
                )
                .collect(Collectors.toList());
    }
    return sortAccommodations(accommodations, sortBy);
}

// sortAccommodations 改為 public 以便 Controller 調用
public List<Accommodation> sortAccommodations(List<Accommodation> accommodations, String sortBy) {
    // ...existing code...
}
```

**功能**:
- ✅ 同時搜尋地點和名稱
- ✅ 模糊匹配
- ✅ 不區分大小寫
- ✅ 支援排序

---

## 📊 搜尋場景對比

### 場景 1: 只搜尋關鍵字

**輸入**:
```
關鍵字: "台北"
日期: (留空)
人數: 2
```

**API 調用**:
```
GET /api/accommodations/search?query=台北
```

**結果**:
- 找到所有地點或名稱包含「台北」的住宿
- 例如：台北商旅、台北經濟旅館

---

### 場景 2: 只選擇日期

**輸入**:
```
關鍵字: (留空)
日期: 2025-11-10 至 2025-11-12
人數: 2
```

**API 調用**:
```
GET /api/accommodations/available?checkIn=2025-11-10&checkOut=2025-11-12
```

**結果**:
- 找到該日期區間內可預訂的所有住宿

---

### 場景 3: 複合搜尋

**輸入**:
```
關鍵字: "日安"
日期: 2025-11-10 至 2025-11-12
人數: 2
排序: price_asc
```

**API 調用**:
```
GET /api/accommodations/search?query=日安&checkIn=2025-11-10&checkOut=2025-11-12&guests=2&sortBy=price_asc
```

**結果**:
- 找到名稱包含「日安」且在該日期可預訂的住宿
- 按價格從低到高排序

---

### 場景 4: 搜尋飯店名稱

**輸入**:
```
關鍵字: "海景villa"
```

**API 調用**:
```
GET /api/accommodations/search?query=海景villa
```

**結果**:
- 找到名稱包含「海景villa」的住宿
- 即使地點不同也能找到

---

## 🎨 UI 展示

### 搜尋區域（完整版）

```
┌───────────────────────────────────────────────────────────────────┐
│  🏨 尋找您的理想住宿                                              │
├───────────────────────────────────────────────────────────────────┤
│ 地點或飯店名稱         入住日期      退房日期      入住人數       │
│ [台北、日安旅館]      [2025-11-10]  [2025-11-11]  [-] [2] [+]   │
│ 輸入城市、地區                                      2 位成人       │
│ 或飯店名稱                                                        │
│                                                                   │
│                                         [🔍 搜尋住宿]            │
│                                         [  清除條件  ]            │
│                                                                   │
│ 搜尋條件: 關鍵字: "台北" | 2025-11-10 - 2025-11-11 (1晚) | 2位成人 │
└───────────────────────────────────────────────────────────────────┘
```

---

### 搜尋結果顯示

```
┌───────────────────────────────────────┐
│ "台北" 的可用住宿                     │
│ 找到 3 間住宿                    排序: [我們的推薦 ▼]    │
├───────────────────────────────────────┤
│ [住宿卡片 1]  [住宿卡片 2]  [住宿卡片 3] │
└───────────────────────────────────────┘
```

---

### 空結果顯示

```
┌───────────────────────────────────────┐
│          😔 找不到符合條件的住宿        │
│          請嘗試調整搜尋條件            │
│                                       │
│          [  清除所有條件  ]            │
└───────────────────────────────────────┘
```

---

## 🧪 測試案例

### 測試 1: 關鍵字搜尋飯店名稱

**步驟**:
1. 訪問首頁
2. 輸入關鍵字「日安」
3. 點擊「搜尋住宿」

**預期結果**:
- ✅ 找到「日安旅館」
- ✅ 顯示搜尋條件：關鍵字: "日安"
- ✅ 標題：「"日安" 的搜尋結果」

---

### 測試 2: 日期範圍搜尋

**步驟**:
1. 選擇入住日期：明天
2. 選擇退房日期：後天
3. 點擊「搜尋住宿」

**預期結果**:
- ✅ 找到該日期可預訂的住宿
- ✅ 顯示搜尋條件：包含日期和天數
- ✅ 標題：「可用住宿」

---

### 測試 3: 複合搜尋

**步驟**:
1. 輸入關鍵字「台北」
2. 選擇日期
3. 人數設為 4
4. 排序選擇「價格：低到高」
5. 點擊「搜尋住宿」

**預期結果**:
- ✅ 找到台北地區該日期可預訂的住宿
- ✅ 按價格排序
- ✅ 顯示完整搜尋條件
- ✅ 標題：「"台北" 的可用住宿」

---

### 測試 4: 人數變更

**步驟**:
1. 點擊人數 [-] 按鈕
2. 點擊人數 [+] 按鈕多次

**預期結果**:
- ✅ 最少為 1 人
- ✅ 最多為 10 人
- ✅ 文字即時更新「X 位成人」

---

### 測試 5: 日期自動調整

**步驟**:
1. 選擇入住日期為某一天
2. 退房日期自動調整為隔天

**預期結果**:
- ✅ 退房日期自動設為入住日期 + 1
- ✅ 退房日期的最小值限制更新

---

### 測試 6: 清除搜尋

**步驟**:
1. 輸入多個搜尋條件
2. 點擊「清除條件」

**預期結果**:
- ✅ 關鍵字清空
- ✅ 人數重置為 2
- ✅ 日期重置為明後天
- ✅ 排序重置
- ✅ 顯示所有住宿

---

### 測試 7: Enter 鍵搜尋

**步驟**:
1. 在關鍵字欄位輸入「台北」
2. 按 Enter 鍵

**預期結果**:
- ✅ 自動執行搜尋
- ✅ 效果等同點擊「搜尋住宿」

---

## 💡 技術亮點

### 1. 智能 API 路由

```javascript
// 根據條件自動選擇最合適的 API
if (query && (checkIn && checkOut)) {
    url = `/api/accommodations/search?${searchParams}`;  // 複合搜尋
} else if (query) {
    url = `/api/accommodations/search?query=${query}`;   // 關鍵字
} else if (checkIn && checkOut) {
    url = `/api/accommodations/available?...`;           // 日期
}
```

---

### 2. 向下兼容

```java
// Controller 同時支援 query 和 location 參數
String searchKeyword = query != null ? query : location;
```

**好處**:
- ✅ 新 API 使用 `query`
- ✅ 舊 API 使用 `location` 依然有效
- ✅ 平滑過渡

---

### 3. 即時回饋

```javascript
// 搜尋條件即時顯示
updateSearchInfo(query, checkIn, checkOut, guests);

// 結果數量即時顯示
resultsCount.textContent = `找到 ${list.length} 間住宿`;
```

---

### 4. 防呆設計

```javascript
// 日期自動調整
if (!checkOut || new Date(checkOut) <= checkInDate) {
    checkOutInput.value = minCheckOut.toISOString().split('T')[0];
}

// 人數限制
const newValue = Math.max(1, Math.min(10, current + delta));
```

---

### 5. 語意化搜尋

```java
// 同時搜尋地點和名稱
filter(acc -> 
    acc.getLocation().toLowerCase().contains(keyword) ||
    acc.getName().toLowerCase().contains(keyword)
)
```

**效果**:
- 搜尋「台北」→ 找到地點在台北的住宿
- 搜尋「日安」→ 找到名稱包含「日安」的住宿
- 更符合用戶直覺

---

## 🚀 未來優化建議

### 1. 房間數量選擇

**目前**: 只有人數選擇  
**建議**: 添加房間數量選擇

```html
<div class="col-md-2">
    <label>房間與人數</label>
    <div>
        <span>房間 [1 ▼]</span>
        <span>成人 [2 ▼]</span>
    </div>
</div>
```

---

### 2. 價格範圍篩選

```html
<div class="col-md-3">
    <label>價格範圍</label>
    <input type="range" min="0" max="10000" value="5000">
    <div>NT$ 0 - NT$ 5000</div>
</div>
```

---

### 3. 設施篩選

```html
<div class="filters">
    <label><input type="checkbox"> WiFi</label>
    <label><input type="checkbox"> 停車場</label>
    <label><input type="checkbox"> 早餐</label>
</div>
```

---

### 4. 地圖整合

```javascript
// 顯示住宿在地圖上的位置
function showMap(accommodations) {
    // 使用 Google Maps API 或 OpenStreetMap
}
```

---

### 5. 搜尋歷史

```javascript
// 儲存最近搜尋
localStorage.setItem('recentSearches', JSON.stringify(searches));
```

---

### 6. 智能建議

```javascript
// 輸入時顯示建議
<datalist id="suggestions">
    <option value="台北">
    <option value="台中">
    <option value="高雄">
</datalist>
```

---

## ✨ 總結

### 實作完成 ✅

- ✅ Booking.com 風格統一搜尋列
- ✅ 地點或飯店名稱搜尋
- ✅ 日期選擇（預設明後天）
- ✅ 人數選擇（1-10 人）
- ✅ 複合搜尋支援
- ✅ 即時搜尋結果提示
- ✅ 漂亮的 UI 設計
- ✅ 後端 API 升級

### 搜尋功能對比

| 功能 | 舊版 | 新版 |
|------|------|------|
| 搜尋方式 | 分散 | 統一 ✅ |
| 支援名稱搜尋 | ❌ | ✅ |
| 支援地點搜尋 | ✅ | ✅ |
| 日期選擇 | 分開 | 整合 ✅ |
| 人數選擇 | ❌ | ✅ |
| 預設日期 | ❌ | 明後天 ✅ |
| 複合條件 | ❌ | ✅ |
| 搜尋提示 | ❌ | ✅ |
| 結果計數 | ❌ | ✅ |
| UI 美觀度 | ⚠️ | ✅ |

### 編譯狀態

```
✅ BUILD SUCCESS
✅ 總時間: 1.930 秒
```

### 修改的檔案

| 檔案 | 變更 | 說明 |
|------|------|------|
| index.html | HTML | 新的搜尋區域 UI |
| index.html | CSS | 漸變背景樣式 |
| index.html | JavaScript | 7 個新函數 |
| AccommodationController.java | API | 統一搜尋端點 |
| BookingService.java | Service | searchByLocationOrName 方法 |

---

**實作日期**: 2025-11-09  
**版本**: 2.0  
**狀態**: ✅ 完整升級完成，Booking.com 風格搜尋


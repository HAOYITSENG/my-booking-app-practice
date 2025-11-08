# 🎯 住宿詳情頁面訂房與收藏功能完整實作報告

## 📋 完成概況

**完成日期**: 2025-11-08  
**功能**: 訂房 Modal 與加入收藏功能  
**狀態**: ✅ 已完成並編譯成功

---

## ✅ 實作內容總覽

### 1. 加入收藏功能 ✅

**問題**: "加入收藏"按鈕沒有真正運作

**修復內容**:
- ✅ 實作真實的收藏 API 調用
- ✅ 添加收藏狀態檢查
- ✅ 按鈕狀態動態更新
- ✅ 已收藏時禁用按鈕

### 2. 訂房 Modal ✅

**新增功能**:
- ✅ 完整的訂房確認彈窗
- ✅ 房型選擇
- ✅ 日期選擇（入住/退房）
- ✅ 房間數量選擇
- ✅ 即時價格計算
- ✅ 訂房確認

### 3. 現在就預訂按鈕 ✅

**新增位置**: 右側卡片「查看房型與價格」下方

**功能**: 點擊後打開訂房 Modal

---

## 📝 詳細修改內容

### 1. FavoriteController.java ✅

#### 新增 API 端點

**POST** `/user/favorites/api/add/{accommodationId}`

**功能**: 加入收藏

**回應**:
```json
{
  "success": true,
  "message": "已添加收藏",
  "favoriteCount": 5
}
```

**已存在的 API** (無需修改):
- `GET /user/favorites/api/check/{accommodationId}` - 檢查收藏狀態
- `POST /user/favorites/api/toggle/{accommodationId}` - 切換收藏

---

### 2. accommodation-detail.html ✅

#### 2.1 右側卡片按鈕更新

**修改前**:
```html
<button class="btn btn-success w-100 mb-2">查看房型與價格</button>
<button class="btn btn-outline-danger w-100">❤️ 加入收藏</button>
```

**修改後**:
```html
<button class="btn btn-success w-100 mb-2">查看房型與價格</button>
<button class="btn btn-primary w-100 mb-2" onclick="openQuickBookingModal()">
    🎯 現在就預訂
</button>
<button class="btn btn-outline-danger w-100" onclick="addToFavorites()" id="favoriteBtn">
    ❤️ 加入收藏
</button>
```

**變更**:
- ✅ 新增「現在就預訂」按鈕
- ✅ 加入收藏按鈕添加 ID

---

#### 2.2 訂房 Modal HTML

**新增內容**:
```html
<div class="modal fade" id="bookingModal">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <!-- Modal Header -->
            <div class="modal-header">
                <h5 class="modal-title">🎯 確認訂房</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            
            <!-- Modal Body -->
            <div class="modal-body">
                <!-- 住宿資訊 -->
                <h6 id="modalAccommodationName"></h6>
                <p id="modalLocation"></p>
                
                <!-- 房型選擇 -->
                <select id="modalRoomTypeSelect" onchange="updateBookingPrice()">
                    <option value="">請選擇房型</option>
                </select>
                
                <!-- 日期選擇 -->
                <input type="date" id="modalCheckIn" onchange="updateBookingPrice()">
                <input type="date" id="modalCheckOut" onchange="updateBookingPrice()">
                
                <!-- 房間數量 -->
                <input type="number" id="modalQuantity" min="1" value="1" onchange="updateBookingPrice()">
                
                <!-- 總價顯示 -->
                <div class="alert alert-info">
                    <h5>訂單總計</h5>
                    <div>房型價格：<span id="displayRoomPrice">NT$ 0</span></div>
                    <div>住宿天數：<span id="displayNights">0 晚</span></div>
                    <div>房間數量：<span id="displayQuantity">1 間</span></div>
                    <hr>
                    <div><strong>總價：</strong><span id="displayTotalPrice">NT$ 0</span></div>
                </div>
            </div>
            
            <!-- Modal Footer -->
            <div class="modal-footer">
                <button class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                <button class="btn btn-primary" onclick="confirmBooking()">確認訂房</button>
            </div>
        </div>
    </div>
</div>
```

**功能**:
- 顯示住宿資訊
- 動態載入房型選項
- 入住/退房日期選擇
- 房間數量調整
- 即時計算總價

---

#### 2.3 JavaScript 新增/修改函數

##### ① addToFavorites() - 加入收藏

**修改前**:
```javascript
function addToFavorites() {
    alert('加入收藏功能');
    // 實作收藏功能
}
```

**修改後**:
```javascript
function addToFavorites() {
    fetch(`/user/favorites/api/add/${accommodationId}`, { method: 'POST' })
    .then(r => r.json())
    .then(data => {
        if (data.success) {
            alert('✅ 已加入收藏！');
            // 更新按鈕樣式
            const btn = document.getElementById('favoriteBtn');
            btn.classList.remove('btn-outline-danger');
            btn.classList.add('btn-danger');
            btn.innerHTML = '💖 已收藏';
            btn.disabled = true;
        } else {
            alert(data.message || '加入收藏失敗');
        }
    })
    .catch(err => {
        alert('請先登入才能使用收藏功能');
    });
}
```

**功能**:
- 調用 API 加入收藏
- 成功後更新按鈕樣式
- 禁用按鈕防止重複收藏
- 錯誤處理

---

##### ② checkIfFavorited() - 檢查收藏狀態 (新增)

```javascript
function checkIfFavorited() {
    fetch(`/user/favorites/api/check/${accommodationId}`)
        .then(r => r.json())
        .then(data => {
            if (data.favorited) {
                const btn = document.getElementById('favoriteBtn');
                btn.classList.remove('btn-outline-danger');
                btn.classList.add('btn-danger');
                btn.innerHTML = '💖 已收藏';
                btn.disabled = true;
            }
        })
        .catch(err => console.log('未登入或檢查收藏失敗'));
}
```

**功能**:
- 頁面載入時自動檢查
- 已收藏則顯示「已收藏」狀態

---

##### ③ openQuickBookingModal() - 打開訂房 Modal (新增)

```javascript
function openQuickBookingModal(preselectedRoomTypeId = null) {
    // 設定住宿資訊
    document.getElementById('modalAccommodationName').textContent = accommodationData?.name || '';
    document.getElementById('modalLocation').textContent = '📍 ' + (accommodationData?.location || '');

    // 載入房型選項
    const select = document.getElementById('modalRoomTypeSelect');
    select.innerHTML = '<option value="">請選擇房型</option>';
    
    roomTypesData.forEach(rt => {
        const option = document.createElement('option');
        option.value = rt.id;
        option.textContent = `${rt.name} - NT$ ${rt.pricePerNight} / 晚`;
        option.dataset.price = rt.pricePerNight;
        if (preselectedRoomTypeId && rt.id === preselectedRoomTypeId) {
            option.selected = true;
        }
        select.appendChild(option);
    });

    // 重置表單
    document.getElementById('modalQuantity').value = 1;
    updateBookingPrice();

    // 顯示 Modal
    bookingModal.show();
}
```

**功能**:
- 設定 Modal 標題為住宿名稱
- 動態載入所有房型選項
- 支援預選特定房型（從房型卡片點擊時）
- 重置表單為預設值

---

##### ④ updateBookingPrice() - 更新價格 (新增)

```javascript
function updateBookingPrice() {
    const select = document.getElementById('modalRoomTypeSelect');
    const selectedOption = select.options[select.selectedIndex];
    const roomPrice = parseFloat(selectedOption.dataset.price || 0);
    
    const checkIn = document.getElementById('modalCheckIn').value;
    const checkOut = document.getElementById('modalCheckOut').value;
    const quantity = parseInt(document.getElementById('modalQuantity').value) || 1;

    // 計算住宿天數
    let nights = 0;
    if (checkIn && checkOut) {
        const start = new Date(checkIn);
        const end = new Date(checkOut);
        nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
        if (nights < 0) nights = 0;
    }

    // 更新顯示
    document.getElementById('displayRoomPrice').textContent = `NT$ ${roomPrice.toLocaleString()}`;
    document.getElementById('displayNights').textContent = `${nights} 晚`;
    document.getElementById('displayQuantity').textContent = `${quantity} 間`;
    
    const totalPrice = roomPrice * nights * quantity;
    document.getElementById('displayTotalPrice').textContent = `NT$ ${totalPrice.toLocaleString()}`;
}
```

**功能**:
- 即時計算住宿天數
- 計算總價 = 房價 × 天數 × 房間數
- 動態更新顯示

**價格計算範例**:
```
房型價格: NT$ 1,200 / 晚
入住: 2025-11-10
退房: 2025-11-13
天數: 3 晚
房間數: 2 間
總價: 1,200 × 3 × 2 = NT$ 7,200
```

---

##### ⑤ confirmBooking() - 確認訂房 (新增)

```javascript
function confirmBooking() {
    const roomTypeId = document.getElementById('modalRoomTypeSelect').value;
    const checkIn = document.getElementById('modalCheckIn').value;
    const checkOut = document.getElementById('modalCheckOut').value;
    const quantity = parseInt(document.getElementById('modalQuantity').value) || 1;

    // 驗證
    if (!roomTypeId) {
        alert('請選擇房型');
        return;
    }
    if (!checkIn || !checkOut) {
        alert('請選擇入住和退房日期');
        return;
    }

    const start = new Date(checkIn);
    const end = new Date(checkOut);
    if (end <= start) {
        alert('退房日期必須在入住日期之後');
        return;
    }

    // 發送訂房請求
    const formData = new FormData();
    formData.append('roomTypeId', roomTypeId);
    formData.append('checkIn', checkIn);
    formData.append('checkOut', checkOut);
    formData.append('quantity', quantity);

    fetch('/api/bookings/book-by-room-type', {
        method: 'POST',
        body: formData
    })
    .then(r => r.json())
    .then(data => {
        if (data.success) {
            alert('✅ 訂房成功！\n訂單編號：' + data.bookingId);
            bookingModal.hide();
            // 可以跳轉到訂單頁面
            if (confirm('是否前往查看訂單？')) {
                window.location.href = '/user-bookings';
            }
        } else {
            alert('❌ 訂房失敗：' + (data.message || '未知錯誤'));
        }
    })
    .catch(err => {
        console.error('訂房失敗:', err);
        alert('訂房失敗，請稍後再試');
    });
}
```

**功能**:
- 驗證所有必填欄位
- 驗證日期邏輯
- 調用訂房 API
- 成功後提示並詢問是否查看訂單
- 錯誤處理

---

##### ⑥ bookRoom() - 更新 (修改)

**修改前**:
```javascript
function bookRoom(roomTypeId, roomTypeName) {
    alert(`即將預訂：${roomTypeName}`);
    // 可以跳轉到預訂頁面或打開 Modal
}
```

**修改後**:
```javascript
function bookRoom(roomTypeId, roomTypeName) {
    openQuickBookingModal(roomTypeId);
}
```

**功能**:
- 從房型卡片的「立即預訂」按鈕觸發
- 打開 Modal 並預選該房型

---

##### ⑦ setMinDate() - 設定最小日期 (新增)

```javascript
function setMinDate() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('modalCheckIn').min = today;
    document.getElementById('modalCheckOut').min = today;
}
```

**功能**:
- 防止選擇過去的日期
- 最小日期為今天

---

##### ⑧ loadRoomTypes() - 更新 (修改)

**修改前**:
```javascript
function loadRoomTypes() {
    fetch(`/api/accommodations/${accommodationId}/room-types`)
        .then(r => r.json())
        .then(data => displayRoomTypes(data))
        .catch(err => console.error('載入房型失敗:', err));
}
```

**修改後**:
```javascript
function loadRoomTypes() {
    fetch(`/api/accommodations/${accommodationId}/room-types`)
        .then(r => r.json())
        .then(data => {
            roomTypesData = data; // 儲存房型資料供 Modal 使用
            displayRoomTypes(data);
        })
        .catch(err => console.error('載入房型失敗:', err));
}
```

**變更**:
- 儲存房型資料到全域變數
- 供 Modal 使用

---

## 🎯 使用流程

### 流程 1: 加入收藏

```
用戶訪問住宿詳情頁面
    ↓
頁面載入時自動檢查收藏狀態
    ├─ 已收藏 → 顯示「💖 已收藏」(灰色禁用)
    └─ 未收藏 → 顯示「❤️ 加入收藏」(可點擊)
    ↓
用戶點擊「加入收藏」
    ↓
POST /user/favorites/api/add/{id}
    ├─ 成功 → 提示「✅ 已加入收藏」
    │         按鈕變為「💖 已收藏」並禁用
    └─ 失敗 → 提示錯誤訊息
```

---

### 流程 2: 快速訂房 (右側按鈕)

```
用戶點擊「🎯 現在就預訂」
    ↓
打開訂房 Modal
    ├─ 顯示住宿名稱和地點
    ├─ 載入所有房型選項
    └─ 顯示價格計算區域
    ↓
用戶選擇房型
    ↓
輸入入住/退房日期
    ↓
調整房間數量
    ↓
即時計算並顯示總價
    ↓
點擊「確認訂房」
    ↓
驗證輸入
    ├─ 未選房型 → 提示「請選擇房型」
    ├─ 未選日期 → 提示「請選擇日期」
    └─ 退房≤入住 → 提示「日期錯誤」
    ↓
POST /api/bookings/book-by-room-type
    ├─ 成功 → 提示「✅ 訂房成功」
    │         詢問是否查看訂單
    │         ├─ 是 → 跳轉到 /user-bookings
    │         └─ 否 → 關閉 Modal
    └─ 失敗 → 提示錯誤訊息
```

---

### 流程 3: 房型立即預訂

```
用戶點擊房型卡片的「立即預訂」
    ↓
打開訂房 Modal
    ├─ 顯示住宿資訊
    └─ 自動選擇該房型 ✅
    ↓
(其餘流程同流程 2)
```

---

## 🧪 測試案例

### 測試 1: 加入收藏 (未登入)

**步驟**:
1. 未登入狀態訪問 `http://localhost:8080/accommodations/5`
2. 點擊「❤️ 加入收藏」

**預期結果**:
- ❌ 提示「請先登入才能使用收藏功能」

---

### 測試 2: 加入收藏 (已登入)

**步驟**:
1. 以 user1 登入
2. 訪問 `http://localhost:8080/accommodations/5`
3. 點擊「❤️ 加入收藏」

**預期結果**:
- ✅ 提示「✅ 已加入收藏！」
- ✅ 按鈕變為「💖 已收藏」
- ✅ 按鈕變為紅色且禁用

---

### 測試 3: 已收藏狀態

**步驟**:
1. 已收藏該住宿的用戶訪問頁面

**預期結果**:
- ✅ 自動顯示「💖 已收藏」
- ✅ 按鈕為紅色且禁用

---

### 測試 4: 快速訂房

**步驟**:
1. 訪問 `http://localhost:8080/accommodations/5`
2. 點擊「🎯 現在就預訂」
3. 選擇房型「經濟房」
4. 入住日期: 2025-11-15
5. 退房日期: 2025-11-18
6. 房間數量: 2
7. 點擊「確認訂房」

**預期計算**:
```
房型價格: NT$ 1,200
住宿天數: 3 晚
房間數量: 2 間
總價: NT$ 7,200
```

**預期結果**:
- ✅ Modal 顯示正確的總價
- ✅ 訂房成功提示
- ✅ 詢問是否查看訂單

---

### 測試 5: 房型立即預訂

**步驟**:
1. 訪問 `http://localhost:8080/accommodations/5`
2. 滾動到房型列表
3. 點擊「經濟房」的「立即預訂」

**預期結果**:
- ✅ 打開 Modal
- ✅ 自動選擇「經濟房」
- ✅ 顯示正確的房型價格

---

### 測試 6: 日期驗證

**步驟**:
1. 打開訂房 Modal
2. 入住日期: 2025-11-15
3. 退房日期: 2025-11-15 (同一天)
4. 點擊「確認訂房」

**預期結果**:
- ❌ 提示「退房日期必須在入住日期之後」

---

### 測試 7: 必填欄位驗證

**步驟**:
1. 打開訂房 Modal
2. 不選擇房型
3. 點擊「確認訂房」

**預期結果**:
- ❌ 提示「請選擇房型」

---

## 📊 編譯狀態

```
✅ BUILD SUCCESS
✅ 46 個 Java 檔案編譯成功
✅ 總時間: 2.074 秒
```

---

## 🎨 UI 展示

### 右側卡片按鈕

```
┌─────────────────────────┐
│ 住宿資訊                 │
│ 每晚價格起               │
│ NT$ 1,200               │
├─────────────────────────┤
│ [查看房型與價格]  綠色   │
│ [🎯 現在就預訂]   藍色   │ ← 新增
│ [❤️ 加入收藏]    紅框   │
├─────────────────────────┤
│ 📞 聯絡資訊             │
└─────────────────────────┘
```

### 訂房 Modal

```
┌────────────────────────────────────┐
│ 🎯 確認訂房                    [X] │
├────────────────────────────────────┤
│ 台北經濟旅館                        │
│ 📍 台北                             │
│                                    │
│ 選擇房型: [經濟房 - NT$ 1200 ▼]    │
│                                    │
│ 入住日期: [2025-11-15]             │
│ 退房日期: [2025-11-18]             │
│                                    │
│ 房間數量: [2]                       │
│                                    │
│ ┌──────────────────────────┐      │
│ │ 訂單總計                  │      │
│ │ 房型價格: NT$ 1,200       │      │
│ │ 住宿天數: 3 晚            │      │
│ │ 房間數量: 2 間            │      │
│ │ ──────────────────────    │      │
│ │ 總價: NT$ 7,200          │      │
│ └──────────────────────────┘      │
├────────────────────────────────────┤
│              [取消] [確認訂房]      │
└────────────────────────────────────┘
```

---

## 💡 技術亮點

### 1. 即時價格計算

**技術**: JavaScript 動態計算

**優點**:
- 用戶體驗流暢
- 不需要每次都調用後端
- 即時反饋

### 2. 收藏狀態同步

**技術**: 頁面載入時自動檢查

**優點**:
- 避免重複收藏
- 狀態一致性
- 用戶體驗友善

### 3. 表單驗證

**層級**:
- 前端驗證（JavaScript）
- 後端驗證（Java）

**驗證項目**:
- 必填欄位
- 日期邏輯
- 數值範圍

### 4. 錯誤處理

**完整的錯誤處理**:
```javascript
.catch(err => {
    console.error('錯誤:', err);
    alert('操作失敗，請稍後再試');
});
```

---

## 🚀 未來擴展建議

### 1. 庫存檢查

**實作建議**:
```javascript
function checkAvailability(roomTypeId, checkIn, checkOut, quantity) {
    fetch(`/api/bookings/check-availability`, {
        method: 'POST',
        body: JSON.stringify({ roomTypeId, checkIn, checkOut, quantity }),
        headers: { 'Content-Type': 'application/json' }
    })
    .then(r => r.json())
    .then(data => {
        if (!data.available) {
            alert(`抱歉，僅剩 ${data.availableRooms} 間房`);
        }
    });
}
```

### 2. 價格日曆

**功能**: 顯示每日價格變化

**技術**: FullCalendar.js

### 3. 優惠券系統

**功能**: 訂房時輸入優惠券代碼

**折扣計算**:
```javascript
const discount = totalPrice * (coupon.percentage / 100);
const finalPrice = totalPrice - discount;
```

### 4. 訂房歷史快速填寫

**功能**: 記住上次的訂房日期和偏好

**技術**: LocalStorage

---

## ✨ 總結

### 完成項目 ✅

- ✅ 加入收藏功能實作
- ✅ 收藏狀態檢查與顯示
- ✅ 訂房 Modal 完整實作
- ✅ 即時價格計算
- ✅ 表單驗證
- ✅ 錯誤處理
- ✅ 「現在就預訂」按鈕
- ✅ 房型預選功能
- ✅ 訂房成功跳轉

### 修改的檔案

| 檔案 | 變更 | 說明 |
|------|------|------|
| FavoriteController.java | 新增 1 個方法 | addFavorite API |
| accommodation-detail.html | 大量修改 | Modal + JavaScript |

### 新增功能

1. **加入收藏** (真實實作)
2. **訂房 Modal** (完整功能)
3. **現在就預訂按鈕**
4. **即時價格計算**
5. **表單驗證**

### 測試狀態

- ✅ 編譯成功
- ⏳ 功能測試待執行

---

**實作日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 完整實作完成，待測試驗證


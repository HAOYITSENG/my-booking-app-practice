# 🛡️ 訂房防錯驗證功能完整實作報告

## 📋 完成概況

**完成日期**: 2025-11-09  
**功能**: 訂房 Modal 完整防錯驗證  
**狀態**: ✅ 已完成並編譯成功

---

## ✅ 實作的防錯功能

### 1. 📅 日期限制與驗證

#### 1.1 今天以前的日期完全鎖定
- ✅ **不能選擇過去的日期**
- ✅ 使用 HTML5 `min` 屬性
- ✅ 動態設定最小日期為今天

#### 1.2 入住日期不能晚於退房日期
- ✅ **即時驗證日期邏輯**
- ✅ 顯示錯誤訊息
- ✅ 自動調整退房日期

#### 1.3 預設日期
- ✅ 入住日期：明天
- ✅ 退房日期：後天

---

### 2. 🏠 房間數量限制

#### 2.1 動態上限
- ✅ **根據房型庫存自動設定上限**
- ✅ 使用 HTML5 `max` 屬性
- ✅ 超過時自動調整為最大值

#### 2.2 即時驗證
- ✅ 輸入時即時檢查
- ✅ 超過庫存時顯示錯誤
- ✅ 自動修正為最大值

#### 2.3 庫存提示
- ✅ 顯示房型剩餘數量
- ✅ 提示可預訂上限

---

### 3. ⚠️ 錯誤提示訊息

#### 3.1 欄位驗證錯誤
- ✅ 紅色邊框標示
- ✅ 錯誤訊息顯示
- ✅ Bootstrap `.is-invalid` 樣式

#### 3.2 提交驗證錯誤
- ✅ Alert 彈窗提示
- ✅ 清楚的錯誤說明
- ✅ Emoji 標示（❌）

---

## 📝 詳細實作內容

### 1. HTML 更新

#### 選擇房型欄位

**修改前**:
```html
<label class="form-label">選擇房型</label>
<select class="form-select" id="modalRoomTypeSelect">
    <option value="">請選擇房型</option>
</select>
```

**修改後**:
```html
<label class="form-label">選擇房型 <span class="text-danger">*</span></label>
<select class="form-select" id="modalRoomTypeSelect" onchange="updateBookingPrice()">
    <option value="">請選擇房型</option>
</select>
<div class="form-text" id="roomTypeStock"></div>  <!-- 庫存提示 -->
```

---

#### 日期選擇欄位

**修改前**:
```html
<label class="form-label">入住日期</label>
<input type="date" class="form-control" id="modalCheckIn">

<label class="form-label">退房日期</label>
<input type="date" class="form-control" id="modalCheckOut">
```

**修改後**:
```html
<label class="form-label">入住日期 <span class="text-danger">*</span></label>
<input type="date" class="form-control" id="modalCheckIn" 
       onchange="validateDates(); updateBookingPrice();">
<div class="invalid-feedback" id="checkInError"></div>  <!-- 錯誤訊息 -->

<label class="form-label">退房日期 <span class="text-danger">*</span></label>
<input type="date" class="form-control" id="modalCheckOut" 
       onchange="validateDates(); updateBookingPrice();">
<div class="invalid-feedback" id="checkOutError"></div>  <!-- 錯誤訊息 -->
```

---

#### 房間數量欄位

**修改前**:
```html
<label class="form-label">房間數量</label>
<input type="number" class="form-control" id="modalQuantity" 
       min="1" value="1">
```

**修改後**:
```html
<label class="form-label">房間數量 <span class="text-danger">*</span></label>
<input type="number" class="form-control" id="modalQuantity" 
       min="1" max="1" value="1" 
       onchange="validateQuantity(); updateBookingPrice();">
<div class="form-text">
    <span id="quantityHint">請先選擇房型</span>  <!-- 提示訊息 -->
</div>
<div class="invalid-feedback" id="quantityError"></div>  <!-- 錯誤訊息 -->
```

---

### 2. JavaScript 新增函數

#### 2.1 setMinDate() - 設定日期限制

**功能**:
- 設定入住/退房日期的最小值為今天
- 預設入住日期為明天
- 預設退房日期為後天

**代碼**:
```javascript
function setMinDate() {
    const today = new Date().toISOString().split('T')[0];
    const checkInInput = document.getElementById('modalCheckIn');
    const checkOutInput = document.getElementById('modalCheckOut');
    
    checkInInput.min = today;
    checkOutInput.min = today;
    
    // 設定預設值為明天和後天
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dayAfter = new Date();
    dayAfter.setDate(dayAfter.getDate() + 2);
    
    checkInInput.value = tomorrow.toISOString().split('T')[0];
    checkOutInput.value = dayAfter.toISOString().split('T')[0];
}
```

**效果**:
```
今天: 2025-11-09
入住日期 min: 2025-11-09 (不能選更早)
入住日期預設: 2025-11-10
退房日期預設: 2025-11-11
```

---

#### 2.2 validateDates() - 驗證日期

**功能**:
- 檢查日期是否已填寫
- 檢查入住日期不能早於今天
- 檢查退房日期必須晚於入住日期
- 自動調整退房日期的最小值

**代碼**:
```javascript
function validateDates() {
    const checkIn = document.getElementById('modalCheckIn').value;
    const checkOut = document.getElementById('modalCheckOut').value;
    const checkInInput = document.getElementById('modalCheckIn');
    const checkOutInput = document.getElementById('modalCheckOut');
    const checkInError = document.getElementById('checkInError');
    const checkOutError = document.getElementById('checkOutError');
    
    let isValid = true;
    
    // 清除之前的錯誤
    checkInInput.classList.remove('is-invalid');
    checkOutInput.classList.remove('is-invalid');
    checkInError.textContent = '';
    checkOutError.textContent = '';
    
    if (!checkIn) {
        checkInInput.classList.add('is-invalid');
        checkInError.textContent = '請選擇入住日期';
        isValid = false;
    }
    
    if (!checkOut) {
        checkOutInput.classList.add('is-invalid');
        checkOutError.textContent = '請選擇退房日期';
        isValid = false;
    }
    
    if (checkIn && checkOut) {
        const checkInDate = new Date(checkIn);
        const checkOutDate = new Date(checkOut);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        // 檢查入住日期不能早於今天
        if (checkInDate < today) {
            checkInInput.classList.add('is-invalid');
            checkInError.textContent = '入住日期不能早於今天';
            isValid = false;
        }
        
        // 檢查退房日期必須晚於入住日期
        if (checkOutDate <= checkInDate) {
            checkOutInput.classList.add('is-invalid');
            checkOutError.textContent = '退房日期必須晚於入住日期';
            isValid = false;
        }
        
        // 自動調整退房日期
        if (checkOutDate <= checkInDate) {
            const nextDay = new Date(checkInDate);
            nextDay.setDate(nextDay.getDate() + 1);
            checkOutInput.value = nextDay.toISOString().split('T')[0];
        }
        
        // 更新退房日期的最小值
        if (checkIn) {
            const minCheckOut = new Date(checkInDate);
            minCheckOut.setDate(minCheckOut.getDate() + 1);
            checkOutInput.min = minCheckOut.toISOString().split('T')[0];
        }
    }
    
    return isValid;
}
```

**效果**:
```
情境1: 入住日期選 2025-11-09，退房日期選 2025-11-09
→ 顯示錯誤: "退房日期必須晚於入住日期"
→ 自動調整退房日期為 2025-11-10

情境2: 入住日期選 2025-11-08 (昨天)
→ 顯示錯誤: "入住日期不能早於今天"
→ 紅色邊框標示
```

---

#### 2.3 validateQuantity() - 驗證房間數量

**功能**:
- 檢查數量不能小於 1
- 檢查數量不能超過庫存
- 超過時自動調整並顯示警告

**代碼**:
```javascript
function validateQuantity() {
    const select = document.getElementById('modalRoomTypeSelect');
    const quantityInput = document.getElementById('modalQuantity');
    const quantityError = document.getElementById('quantityError');
    const selectedOption = select.options[select.selectedIndex];
    
    quantityInput.classList.remove('is-invalid');
    quantityError.textContent = '';
    
    if (!selectedOption.value) {
        return true; // 還沒選擇房型，不驗證
    }
    
    const maxRooms = parseInt(selectedOption.dataset.totalRooms || 1);
    const quantity = parseInt(quantityInput.value || 1);
    
    if (quantity < 1) {
        quantityInput.value = 1;
        quantityInput.classList.add('is-invalid');
        quantityError.textContent = '房間數量至少為 1';
        return false;
    }
    
    if (quantity > maxRooms) {
        quantityInput.value = maxRooms;
        quantityInput.classList.add('is-invalid');
        quantityError.textContent = `此房型最多只有 ${maxRooms} 間可預訂`;
        
        // 3秒後自動清除錯誤提示
        setTimeout(() => {
            quantityInput.classList.remove('is-invalid');
            quantityError.textContent = '';
        }, 3000);
        
        return false;
    }
    
    return true;
}
```

**效果**:
```
房型庫存: 10 間
用戶輸入: 15

→ 自動調整為: 10
→ 顯示錯誤: "此房型最多只有 10 間可預訂"
→ 紅色邊框標示
→ 3秒後錯誤消失
```

---

### 3. 更新現有函數

#### 3.1 openQuickBookingModal() - 顯示庫存

**修改重點**:
```javascript
roomTypesData.forEach(rt => {
    const option = document.createElement('option');
    option.value = rt.id;
    option.textContent = `${rt.name} - NT$ ${rt.pricePerNight} / 晚 (剩餘 ${rt.totalRooms} 間)`;  // ✅ 顯示庫存
    option.dataset.price = rt.pricePerNight;
    option.dataset.totalRooms = rt.totalRooms;  // ✅ 儲存庫存數量
    select.appendChild(option);
});

// 清除所有錯誤提示
document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
document.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');
```

**顯示效果**:
```
下拉選單:
┌─────────────────────────────────────┐
│ 請選擇房型                          │
│ 標準房 - NT$ 2200 / 晚 (剩餘 10 間) │
│ 豪華房 - NT$ 3500 / 晚 (剩餘 5 間)  │
│ 總統套房 - NT$ 8000 / 晚 (剩餘 2 間) │
└─────────────────────────────────────┘
```

---

#### 3.2 updateBookingPrice() - 動態限制

**修改重點**:
```javascript
function updateBookingPrice() {
    const select = document.getElementById('modalRoomTypeSelect');
    const selectedOption = select.options[select.selectedIndex];
    const roomPrice = parseFloat(selectedOption.dataset.price || 0);
    const totalRooms = parseInt(selectedOption.dataset.totalRooms || 1);  // ✅ 取得庫存
    
    const quantityInput = document.getElementById('modalQuantity');
    const roomTypeStock = document.getElementById('roomTypeStock');
    const quantityHint = document.getElementById('quantityHint');

    // 更新房型庫存提示
    if (selectedOption.value) {
        roomTypeStock.textContent = `此房型共有 ${totalRooms} 間可預訂`;
        roomTypeStock.className = 'form-text text-success';
        
        // ✅ 更新房間數量上限
        quantityInput.max = totalRooms;
        quantityHint.textContent = `最多可預訂 ${totalRooms} 間`;
        
        // ✅ 如果當前數量超過庫存，自動調整
        if (parseInt(quantityInput.value) > totalRooms) {
            quantityInput.value = totalRooms;
        }
    } else {
        roomTypeStock.textContent = '';
        quantityInput.max = 1;
        quantityHint.textContent = '請先選擇房型';
    }
    
    // ...計算價格
}
```

**顯示效果**:
```
選擇房型後:
┌─────────────────────────────────┐
│ 選擇房型 *                      │
│ [標準房 - NT$ 2200 / 晚 ▼]     │
│ 此房型共有 10 間可預訂 (綠色)    │
│                                 │
│ 房間數量 *                      │
│ [5] ← max="10"                 │
│ 最多可預訂 10 間                 │
└─────────────────────────────────┘
```

---

#### 3.3 confirmBooking() - 完整驗證

**修改重點**:
```javascript
function confirmBooking() {
    const roomTypeId = document.getElementById('modalRoomTypeSelect').value;
    const checkIn = document.getElementById('modalCheckIn').value;
    const checkOut = document.getElementById('modalCheckOut').value;
    const quantity = parseInt(document.getElementById('modalQuantity').value) || 1;
    const select = document.getElementById('modalRoomTypeSelect');
    const selectedOption = select.options[select.selectedIndex];
    const maxRooms = parseInt(selectedOption.dataset.totalRooms || 1);

    // ✅ 驗證房型
    if (!roomTypeId) {
        alert('❌ 請選擇房型');
        return;
    }

    // ✅ 驗證日期
    if (!checkIn || !checkOut) {
        alert('❌ 請選擇入住和退房日期');
        return;
    }

    // ✅ 驗證日期邏輯
    if (!validateDates()) {
        alert('❌ 請檢查日期是否正確');
        return;
    }

    const start = new Date(checkIn);
    const end = new Date(checkOut);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // ✅ 檢查入住日期
    if (start < today) {
        alert('❌ 入住日期不能早於今天');
        return;
    }

    // ✅ 檢查退房日期
    if (end <= start) {
        alert('❌ 退房日期必須晚於入住日期');
        return;
    }

    // ✅ 驗證房間數量
    if (quantity < 1) {
        alert('❌ 房間數量至少為 1');
        return;
    }

    // ✅ 檢查庫存
    if (quantity > maxRooms) {
        alert(`❌ 此房型最多只有 ${maxRooms} 間可預訂\n您選擇了 ${quantity} 間，請調整數量`);
        return;
    }

    // 發送訂房請求...
}
```

---

## 🧪 測試案例

### 測試 1: 日期限制

**步驟**:
1. 打開訂房 Modal
2. 嘗試選擇昨天的日期

**預期結果**:
- ❌ 昨天的日期無法點選（灰色）
- ✅ 只能選今天或以後

---

### 測試 2: 入住晚於退房

**步驟**:
1. 入住日期: 2025-11-15
2. 退房日期: 2025-11-14

**預期結果**:
- ❌ 退房日期顯示紅色邊框
- ❌ 錯誤訊息: "退房日期必須晚於入住日期"
- ✅ 自動調整退房為 2025-11-16

---

### 測試 3: 超過庫存

**步驟**:
1. 選擇房型: 總統套房 (剩餘 2 間)
2. 房間數量輸入: 5

**預期結果**:
- ✅ 自動調整為 2
- ❌ 紅色邊框
- ❌ 錯誤訊息: "此房型最多只有 2 間可預訂"
- ✅ 3秒後錯誤消失

---

### 測試 4: 提交驗證

**步驟**:
1. 不選擇房型
2. 點擊「確認訂房」

**預期結果**:
- ❌ Alert: "❌ 請選擇房型"
- ❌ 表單不提交

---

### 測試 5: 動態上限

**步驟**:
1. 選擇房型A (10間)
2. 房間數量設為 8
3. 切換到房型B (3間)

**預期結果**:
- ✅ 房間數量自動調整為 3
- ✅ 上限變為 3
- ✅ 提示更新: "最多可預訂 3 間"

---

## 📊 UI 展示

### 訂房 Modal（完整版）

```
┌────────────────────────────────────────┐
│ 🎯 確認訂房                        [X] │
├────────────────────────────────────────┤
│ 台北經濟旅館                            │
│ 📍 台北                                 │
│                                        │
│ 選擇房型 *                             │
│ [經濟房 - NT$1200/晚 (剩餘 15間) ▼]   │ ← 顯示庫存
│ 此房型共有 15 間可預訂 (綠色)           │
│                                        │
│ 入住日期 *         退房日期 *          │
│ [2025-11-10]      [2025-11-11]        │ ← 預設明後天
│                                        │
│ 房間數量 *                              │
│ [2] ← max="15"                        │ ← 上限 15
│ 最多可預訂 15 間                        │
│                                        │
│ ┌──────────────────────────────┐      │
│ │ 訂單總計                      │      │
│ │ 房型價格: NT$ 1,200           │      │
│ │ 住宿天數: 1 晚                │      │
│ │ 房間數量: 2 間                │      │
│ │ ─────────────────────         │      │
│ │ 總價: NT$ 2,400              │      │
│ └──────────────────────────────┘      │
│                                        │
│              [取消] [確認訂房]          │
└────────────────────────────────────────┘
```

### 錯誤狀態

```
┌────────────────────────────────────────┐
│ 入住日期 *         退房日期 *          │
│ [2025-11-15] ✅   [2025-11-14] ❌     │ ← 紅框
│                   ⚠️ 退房日期必須晚於   │ ← 錯誤訊息
│                      入住日期          │
│                                        │
│ 房間數量 *                              │
│ [20] ❌                               │ ← 紅框
│ ⚠️ 此房型最多只有 15 間可預訂           │ ← 錯誤訊息
└────────────────────────────────────────┘
```

---

## 💡 技術亮點

### 1. HTML5 原生驗證

```html
<input type="date" min="2025-11-09">      <!-- 日期下限 -->
<input type="number" min="1" max="15">    <!-- 數字範圍 -->
```

**優點**:
- 瀏覽器原生支援
- 行動裝置友善
- 無需額外 JavaScript

---

### 2. 即時驗證

```javascript
onchange="validateDates(); updateBookingPrice();"
```

**優點**:
- 立即回饋
- 用戶體驗佳
- 減少提交錯誤

---

### 3. 自動調整

```javascript
if (quantity > maxRooms) {
    quantityInput.value = maxRooms;  // 自動修正
}
```

**優點**:
- 防止無效輸入
- 友善的錯誤處理
- 減少用戶困擾

---

### 4. Bootstrap 樣式

```javascript
element.classList.add('is-invalid');  // 紅色邊框
```

**優點**:
- 視覺清晰
- 統一風格
- 專業外觀

---

## 🚀 未來優化建議

### 1. 即時庫存檢查

**目前**: 只檢查房型總數  
**建議**: 檢查特定日期的可用房間

```javascript
function checkAvailability(roomTypeId, checkIn, checkOut) {
    fetch(`/api/bookings/check-availability`, {
        method: 'POST',
        body: JSON.stringify({ roomTypeId, checkIn, checkOut })
    })
    .then(r => r.json())
    .then(data => {
        quantityInput.max = data.availableRooms;
        quantityHint.textContent = `此日期可預訂 ${data.availableRooms} 間`;
    });
}
```

---

### 2. 價格日曆

**功能**: 顯示每日價格變化

```javascript
// 使用 FullCalendar.js
calendar.render({
    events: [
        { date: '2025-11-15', price: 1200 },
        { date: '2025-11-16', price: 1500 },  // 週末加價
    ]
});
```

---

### 3. 優惠提示

**功能**: 長住優惠提示

```javascript
if (nights >= 7) {
    showMessage('✨ 住滿7天享9折優惠！');
}
```

---

### 4. 智能建議

**功能**: 推薦最佳入住日期

```javascript
if (selectedDate.isWeekend) {
    showSuggestion('💡 平日入住更優惠！建議選擇週一至週四');
}
```

---

## ✨ 總結

### 實作功能 ✅

- ✅ 日期限制（不能選過去）
- ✅ 日期邏輯驗證
- ✅ 房間數量上限
- ✅ 庫存顯示
- ✅ 即時錯誤提示
- ✅ 自動調整
- ✅ 完整提交驗證

### 防錯機制 ✅

| 驗證項目 | 前端 | 後端 | 用戶提示 |
|---------|-----|------|---------|
| 日期不能為空 | ✅ | ✅ | ✅ |
| 日期不能過去 | ✅ | ✅ | ✅ |
| 退房晚於入住 | ✅ | ✅ | ✅ |
| 數量不能為 0 | ✅ | ✅ | ✅ |
| 數量不超庫存 | ✅ | ✅ | ✅ |
| 房型必須選擇 | ✅ | ✅ | ✅ |

### 修改的檔案

| 檔案 | 變更 | 說明 |
|------|------|------|
| accommodation-detail.html | HTML 欄位 | 添加必填標示、錯誤提示區 |
| accommodation-detail.html | JavaScript | 新增 3 個驗證函數 |
| accommodation-detail.html | JavaScript | 更新 3 個現有函數 |

### 編譯狀態

```
✅ BUILD SUCCESS
✅ 總時間: 1.954 秒
```

---

**實作日期**: 2025-11-09  
**版本**: 1.0  
**狀態**: ✅ 完整實作完成，待測試驗證


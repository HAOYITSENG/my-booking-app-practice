# 🎯 首頁訂房 Modal 升級報告（與詳情頁完全一致）

## 📋 更新概況

**完成日期**: 2025-11-09  
**功能**: 首頁「快速訂房」Modal 升級為與詳情頁完全相同的版本  
**狀態**: ✅ 已完成並編譯成功

---

## ✅ 升級內容

### 從簡化版升級到完整版

#### 升級前（舊版）
```
簡化的訂房 Modal：
- 基本房型選擇
- 日期選擇
- 房間數量
- 簡單的總價計算
- 缺少驗證功能
```

#### 升級後（新版）
```
完整的訂房 Modal：
✅ 房型選擇（顯示庫存）
✅ 日期選擇（鎖定過去日期）
✅ 房間數量（動態上限）
✅ 即時驗證
✅ 錯誤提示
✅ 自動調整
✅ 詳細總價顯示
```

---

## 📝 詳細變更

### 1. HTML Modal 結構

#### 舊版 Modal
```html
<div class="modal fade" id="bookingModal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">確認訂房</h5>
            </div>
            <div class="modal-body">
                <div id="bookingDetails"></div>
                
                <div class="mb-3">
                    <label>選擇房型</label>
                    <select id="roomTypeSelect" class="form-select"></select>
                </div>
                
                <div class="mb-3">
                    <label>房間數量</label>
                    <input id="quantity" type="number" value="1" min="1">
                </div>
                
                <div class="row">
                    <div class="col-md-6">
                        <label>入住日期</label>
                        <input type="date" id="modalCheckIn">
                    </div>
                    <div class="col-md-6">
                        <label>退房日期</label>
                        <input type="date" id="modalCheckOut">
                    </div>
                </div>
                
                <div class="mt-3">
                    <strong>預估總價：</strong>
                    <span id="estimatedTotal">請選擇日期與房型</span>
                </div>
            </div>
        </div>
    </div>
</div>
```

**問題**:
- ❌ 沒有必填標示
- ❌ 沒有庫存提示
- ❌ 沒有錯誤訊息區域
- ❌ 總價顯示不詳細

---

#### 新版 Modal（完整版）
```html
<div class="modal fade" id="bookingModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">🎯 確認訂房</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <!-- 住宿資訊 -->
                <div class="mb-3">
                    <h6 id="modalAccommodationName" class="text-primary"></h6>
                    <p class="text-muted" id="modalLocation"></p>
                </div>

                <!-- 選擇房型 -->
                <div class="mb-3">
                    <label class="form-label">選擇房型 <span class="text-danger">*</span></label>
                    <select class="form-select" id="modalRoomTypeSelect" onchange="updateBookingPrice()">
                        <option value="">請選擇房型</option>
                    </select>
                    <div class="form-text" id="roomTypeStock"></div>  <!-- ✅ 庫存提示 -->
                </div>

                <!-- 日期選擇 -->
                <div class="row mb-3">
                    <div class="col-md-6">
                        <label class="form-label">入住日期 <span class="text-danger">*</span></label>
                        <input type="date" class="form-control" id="modalCheckIn" 
                               onchange="validateDates(); updateBookingPrice();">
                        <div class="invalid-feedback" id="checkInError"></div>  <!-- ✅ 錯誤提示 -->
                    </div>
                    <div class="col-md-6">
                        <label class="form-label">退房日期 <span class="text-danger">*</span></label>
                        <input type="date" class="form-control" id="modalCheckOut" 
                               onchange="validateDates(); updateBookingPrice();">
                        <div class="invalid-feedback" id="checkOutError"></div>  <!-- ✅ 錯誤提示 -->
                    </div>
                </div>

                <!-- 房間數量 -->
                <div class="mb-3">
                    <label class="form-label">房間數量 <span class="text-danger">*</span></label>
                    <input type="number" class="form-control" id="modalQuantity" 
                           min="1" max="1" value="1" 
                           onchange="validateQuantity(); updateBookingPrice();">
                    <div class="form-text">
                        <span id="quantityHint">請先選擇房型</span>  <!-- ✅ 提示訊息 -->
                    </div>
                    <div class="invalid-feedback" id="quantityError"></div>  <!-- ✅ 錯誤提示 -->
                </div>

                <!-- 總價顯示 -->
                <div class="alert alert-info">
                    <h5>訂單總計</h5>
                    <div class="d-flex justify-content-between">
                        <span>房型價格：</span>
                        <span id="displayRoomPrice">NT$ 0</span>  <!-- ✅ 詳細顯示 -->
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>住宿天數：</span>
                        <span id="displayNights">0 晚</span>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>房間數量：</span>
                        <span id="displayQuantity">1 間</span>
                    </div>
                    <hr>
                    <div class="d-flex justify-content-between">
                        <strong>總價：</strong>
                        <strong class="text-primary" id="displayTotalPrice">NT$ 0</strong>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                <button type="button" class="btn btn-primary" onclick="confirmBooking()">確認訂房</button>
            </div>
        </div>
    </div>
</div>
```

**改進**:
- ✅ 必填標示（*）
- ✅ 庫存提示區域
- ✅ 錯誤訊息區域
- ✅ 詳細總價拆分
- ✅ 即時驗證綁定

---

### 2. JavaScript 函數

#### 新增函數

##### 2.1 CSRF Token 支援
```javascript
// 獲取 CSRF token
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

// 創建帶 CSRF token 的 fetch 選項
function getFetchOptions(method = 'GET', body = null) {
    const options = {
        method: method,
        headers: {}
    };
    
    if (csrfToken && csrfHeader) {
        options.headers[csrfHeader] = csrfToken;
    }
    
    if (body) {
        if (body instanceof FormData) {
            options.body = body;
        } else {
            options.headers['Content-Type'] = 'application/json';
            options.body = JSON.stringify(body);
        }
    }
    
    return options;
}
```

---

##### 2.2 setMinDate() - 設定日期限制
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

**功能**:
- ✅ 鎖定過去日期
- ✅ 預設明天入住
- ✅ 預設後天退房

---

##### 2.3 validateDates() - 驗證日期
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

**功能**:
- ✅ 檢查必填
- ✅ 檢查過去日期
- ✅ 檢查邏輯錯誤
- ✅ 自動調整
- ✅ 顯示錯誤

---

##### 2.4 validateQuantity() - 驗證房間數量
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
        
        setTimeout(() => {
            quantityInput.classList.remove('is-invalid');
            quantityError.textContent = '';
        }, 3000);
        
        return false;
    }
    
    return true;
}
```

**功能**:
- ✅ 檢查最小值
- ✅ 檢查庫存上限
- ✅ 自動調整
- ✅ 錯誤提示

---

#### 更新的函數

##### 3.1 openBookingModal()

**舊版**:
```javascript
function openBookingModal(accId, name, location) {
    currentAccId = accId;
    document.getElementById('bookingDetails').innerHTML = `
        <h6>住宿資訊</h6>
        <p><strong>名稱:</strong> ${name}</p>
        <p><strong>地點:</strong> ${location}</p>
    `;
    
    loadRoomTypes(accId).then(() => {
        bookingModal.show();
        updateEstimatedTotal();
    });
}
```

**新版**:
```javascript
function openBookingModal(accId, name, location) {
    currentAccId = accId;
    
    // 設定住宿資訊
    document.getElementById('modalAccommodationName').textContent = name;
    document.getElementById('modalLocation').textContent = '📍 ' + location;

    // 載入房型
    loadRoomTypes(accId).then(() => {
        // 清除所有錯誤提示
        document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        document.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');
        
        bookingModal.show();
        updateBookingPrice();  // ✅ 使用新的函數
    });
}
```

**改進**:
- ✅ 使用新的元素 ID
- ✅ 清除錯誤狀態
- ✅ 調用新的價格更新函數

---

##### 3.2 fillRoomTypeSelect()

**舊版**:
```javascript
function fillRoomTypeSelect(list) {
    const sel = document.getElementById('roomTypeSelect');
    sel.innerHTML = '<option value="">請選擇房型</option>' +
        list.map(rt => 
            `<option value="${rt.id}" data-price="${rt.pricePerNight}">
                ${rt.name}｜NT$ ${rt.pricePerNight}｜庫存 ${rt.totalRooms}
            </option>`
        ).join('');
}
```

**新版**:
```javascript
function fillRoomTypeSelect(list) {
    const sel = document.getElementById('modalRoomTypeSelect');  // ✅ 新 ID
    if (!list || list.length === 0) {
        sel.innerHTML = '<option value="">此住宿尚無房型</option>';
        return;
    }
    sel.innerHTML = '<option value="">請選擇房型</option>' +
        list.map(rt => `
            <option value="${rt.id}" 
                    data-price="${rt.pricePerNight || 0}" 
                    data-total-rooms="${rt.totalRooms || 1}">  <!-- ✅ 儲存庫存 -->
                ${rt.name} - NT$ ${rt.pricePerNight || 0} / 晚 (剩餘 ${rt.totalRooms || 0} 間)
            </option>
        `).join('');
}
```

**改進**:
- ✅ 使用新的 select ID
- ✅ 儲存庫存數據
- ✅ 顯示剩餘數量

---

##### 3.3 updateBookingPrice() - 全新函數

**取代舊的** `updateEstimatedTotal()`

```javascript
function updateBookingPrice() {
    const select = document.getElementById('modalRoomTypeSelect');
    const selectedOption = select.options[select.selectedIndex];
    const roomPrice = parseFloat(selectedOption.dataset.price || 0);
    const totalRooms = parseInt(selectedOption.dataset.totalRooms || 1);
    
    const quantityInput = document.getElementById('modalQuantity');
    const roomTypeStock = document.getElementById('roomTypeStock');
    const quantityHint = document.getElementById('quantityHint');

    // 更新房型庫存提示
    if (selectedOption.value) {
        roomTypeStock.textContent = `此房型共有 ${totalRooms} 間可預訂`;
        roomTypeStock.className = 'form-text text-success';
        
        // 更新房間數量上限
        quantityInput.max = totalRooms;
        quantityHint.textContent = `最多可預訂 ${totalRooms} 間`;
        
        // 如果當前數量超過庫存，自動調整
        if (parseInt(quantityInput.value) > totalRooms) {
            quantityInput.value = totalRooms;
        }
    } else {
        roomTypeStock.textContent = '';
        quantityInput.max = 1;
        quantityHint.textContent = '請先選擇房型';
    }

    const checkIn = document.getElementById('modalCheckIn').value;
    const checkOut = document.getElementById('modalCheckOut').value;
    const quantity = parseInt(quantityInput.value) || 1;

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
- ✅ 動態調整數量上限
- ✅ 顯示庫存提示
- ✅ 詳細價格拆分
- ✅ 即時計算總價

---

##### 3.4 confirmBooking() - 完整驗證版本

**舊版**:
```javascript
function confirmBooking() {
    const roomTypeId = document.getElementById('roomTypeSelect').value;
    const quantity = Math.max(1, Number(document.getElementById('quantity').value || 1));
    const checkIn = document.getElementById('modalCheckIn').value;
    const checkOut = document.getElementById('modalCheckOut').value;

    if (!roomTypeId) return showAlert('請選擇房型', 'warning');
    if (!checkIn || !checkOut) return showAlert('請選擇入住和退房日期', 'warning');
    if (checkIn >= checkOut) return showAlert('入住日期必須早於退房日期', 'warning');

    fetch(`/api/bookings/by-room-type?...`, { method: 'POST' })
        .then(...)
        .then(() => showAlert('訂房成功', 'success'));
}
```

**新版**:
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

    // 發送訂房請求
    const formData = new FormData();
    formData.append('roomTypeId', roomTypeId);
    formData.append('checkIn', checkIn);
    formData.append('checkOut', checkOut);
    formData.append('quantity', quantity);

    fetch('/api/bookings/book-by-room-type', getFetchOptions('POST', formData))
    .then(r => r.json())
    .then(data => {
        if (data.success) {
            alert('✅ 訂房成功！\n訂單編號：' + data.bookingId);
            bookingModal.hide();
            if (confirm('是否前往查看訂單？')) {
                window.location.href = '/user-bookings';
            }
        } else {
            alert('❌ 訂房失敗：' + (data.message || '未知錯誤'));
        }
    })
    .catch(err => {
        console.error('訂房失敗:', err);
        alert('❌ 訂房失敗，請稍後再試');
    });
}
```

**改進**:
- ✅ 完整驗證邏輯
- ✅ 檢查庫存上限
- ✅ CSRF token 支援
- ✅ 清楚的錯誤訊息

---

## 📊 功能對比

### 舊版 vs 新版

| 功能 | 舊版 | 新版 | 說明 |
|------|------|------|------|
| 日期鎖定 | ❌ | ✅ | 過去日期無法選擇 |
| 預設日期 | ❌ | ✅ | 明天/後天 |
| 日期驗證 | ⚠️ 簡單 | ✅ 完整 | 多重檢查 |
| 庫存顯示 | ⚠️ 靜態 | ✅ 動態 | 剩餘數量 |
| 數量上限 | ❌ | ✅ | 根據庫存 |
| 數量驗證 | ❌ | ✅ | 即時檢查 |
| 錯誤提示 | ⚠️ alert | ✅ 即時 | 紅框+訊息 |
| 自動調整 | ❌ | ✅ | 超過時修正 |
| 總價顯示 | ⚠️ 簡單 | ✅ 詳細 | 拆分明細 |
| CSRF 支援 | ⚠️ 部分 | ✅ 完整 | 自動添加 |

---

## 🧪 測試案例

### 測試 1: 首頁快速訂房

**步驟**:
1. 訪問首頁 `http://localhost:8080/`
2. 點擊任一住宿的「🔍 快速訂房」按鈕

**預期結果**:
- ✅ 打開訂房 Modal
- ✅ 顯示住宿名稱和地點
- ✅ 預設日期為明天和後天
- ✅ 房型下拉選單顯示庫存

---

### 測試 2: 庫存限制

**步驟**:
1. 選擇房型「經濟房 (剩餘 10 間)」
2. 房間數量輸入 15

**預期結果**:
- ✅ 自動調整為 10
- ✅ 紅色邊框
- ✅ 錯誤訊息: "此房型最多只有 10 間可預訂"
- ✅ 3秒後錯誤消失

---

### 測試 3: 日期驗證

**步驟**:
1. 入住日期選擇明天
2. 退房日期選擇明天（同一天）

**預期結果**:
- ✅ 退房日期紅色邊框
- ✅ 錯誤: "退房日期必須晚於入住日期"
- ✅ 自動調整為後天

---

### 測試 4: 訂房成功

**步驟**:
1. 選擇房型
2. 選擇日期（明天至後天）
3. 房間數量 2
4. 點擊「確認訂房」

**預期結果**:
- ✅ 提示: "✅ 訂房成功！訂單編號：XX"
- ✅ 詢問是否查看訂單
- ✅ 點擊「確定」跳轉到訂單頁面

---

## 📊 UI 展示

### 首頁訂房 Modal（完整版）

```
┌──────────────────────────────────┐
│ 🎯 確認訂房                  [X] │
├──────────────────────────────────┤
│ 台北經濟旅館                     │
│ 📍 台北                          │
│                                  │
│ 選擇房型 *                       │
│ [經濟房-NT$1200/晚(剩15間) ▼]   │ ← 顯示庫存
│ 此房型共有 15 間可預訂 ✅         │
│                                  │
│ 入住 *          退房 *           │
│ [2025-11-10]   [2025-11-11]     │ ← 預設明後天
│                                  │
│ 房間數量 *                       │
│ [2] ← max="15"                  │ ← 動態上限
│ 最多可預訂 15 間                  │
│                                  │
│ ┌────────────────────────┐      │
│ │ 訂單總計                │      │
│ │ 房型價格: NT$ 1,200    │      │
│ │ 住宿天數: 1 晚         │      │
│ │ 房間數量: 2 間         │      │
│ │ ───────────────        │      │
│ │ 總價: NT$ 2,400       │      │
│ └────────────────────────┘      │
│                                  │
│         [取消] [確認訂房]         │
└──────────────────────────────────┘
```

---

## ✨ 總結

### 升級完成 ✅

- ✅ HTML Modal 結構完全相同
- ✅ 所有驗證函數已複製
- ✅ CSRF token 支援
- ✅ 日期限制與驗證
- ✅ 庫存顯示與限制
- ✅ 錯誤提示與自動調整
- ✅ 詳細總價顯示

### 防錯機制 ✅

| 驗證項目 | 首頁 | 詳情頁 | 一致性 |
|---------|------|--------|--------|
| 日期不能過去 | ✅ | ✅ | ✅ |
| 退房晚於入住 | ✅ | ✅ | ✅ |
| 數量不超庫存 | ✅ | ✅ | ✅ |
| 房型必須選擇 | ✅ | ✅ | ✅ |
| CSRF token | ✅ | ✅ | ✅ |
| 錯誤提示樣式 | ✅ | ✅ | ✅ |

### 編譯狀態

```
✅ BUILD SUCCESS
✅ 總時間: 1.921 秒
```

### 修改的檔案

| 檔案 | 變更 | 說明 |
|------|------|------|
| index.html | Modal HTML | 完全替換為詳情頁版本 |
| index.html | JavaScript | 新增 4 個驗證函數 |
| index.html | JavaScript | 更新 4 個現有函數 |

---

**實作日期**: 2025-11-09  
**版本**: 2.0  
**狀態**: ✅ 完整升級完成，與詳情頁完全一致


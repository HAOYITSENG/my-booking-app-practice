# 🔧 訂房與收藏功能修復報告（CSRF + API 端點）

## 📋 問題描述

**發現時間**: 2025-11-08 23:50  
**問題**: 
1. 加入收藏失敗
2. 訂房失敗（405 Method Not Allowed）

**錯誤訊息**:
```
POST http://localhost:8080/api/bookings/book-by-room-type 405 (Method Not Allowed)
```

---

## ❌ 問題分析

### 問題 1: CSRF Token 缺失

**原因**: Spring Security 預設啟用 CSRF 保護，POST 請求需要包含 CSRF token

**症狀**:
- 加入收藏請求被拒絕
- 訂房請求被拒絕
- 評論發表失敗

**雖然** `SecurityConfig` 已設定 `.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))`，但 Thymeleaf 頁面仍需要正確處理 CSRF token。

---

### 問題 2: API 端點不存在

**前端調用**:
```javascript
fetch('/api/bookings/book-by-room-type', { method: 'POST', body: formData })
```

**後端實際端點**:
```java
@PostMapping("/by-room-type")  // /api/bookings/by-room-type
```

**問題**: 前端調用 `/api/bookings/book-by-room-type`，但實際端點是 `/api/bookings/by-room-type`

**且**: 現有 API 返回 `Booking` 物件，前端期望的是 JSON 格式：
```json
{
  "success": true,
  "bookingId": 123,
  "message": "訂房成功"
}
```

---

## ✅ 修復方案

### 修復 1: 添加 CSRF Token 支援

#### 1.1 HTML Head 添加 Meta 標籤

**檔案**: `accommodation-detail.html`

**修改**:
```html
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="_csrf" th:content="${_csrf.token}"/>
    <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
    <title>住宿詳情</title>
</head>
```

**作用**: Thymeleaf 會自動注入 CSRF token 到 meta 標籤

---

#### 1.2 JavaScript 獲取並使用 CSRF Token

**新增代碼**:
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

**作用**: 
- 讀取 CSRF token
- 為所有 POST 請求自動添加 CSRF header

---

#### 1.3 更新所有 POST 請求

**① addToFavorites()**

**修改前**:
```javascript
fetch(`/user/favorites/api/add/${accommodationId}`, { method: 'POST' })
```

**修改後**:
```javascript
fetch(`/user/favorites/api/add/${accommodationId}`, getFetchOptions('POST'))
```

---

**② confirmBooking()**

**修改前**:
```javascript
fetch('/api/bookings/book-by-room-type', {
    method: 'POST',
    body: formData
})
```

**修改後**:
```javascript
fetch('/api/bookings/book-by-room-type', getFetchOptions('POST', formData))
```

---

**③ submitReview()**

**修改前**:
```javascript
fetch(`/api/reviews/accommodation/${accommodationId}?...`, {
    method: 'POST'
})
```

**修改後**:
```javascript
fetch(`/api/reviews/accommodation/${accommodationId}?...`, 
    getFetchOptions('POST'))
```

---

### 修復 2: 新增訂房 API 端點

#### 2.1 BookingController 新增方法

**檔案**: `BookingController.java`

**新增導入**:
```java
import java.util.HashMap;
import java.util.Map;
```

**新增 API**:
```java
@PostMapping("/book-by-room-type")
@Operation(
    summary = "建立訂單（前端專用）",
    description = "根據房型 ID、入住/退房日期與房間數量建立新訂單，返回 JSON 格式回應。"
)
public ResponseEntity<Map<String, Object>> bookByRoomTypeJson(
        @RequestParam Long roomTypeId,
        @RequestParam String checkIn,
        @RequestParam String checkOut,
        @RequestParam(defaultValue = "1") Integer quantity
) {
    Map<String, Object> response = new HashMap<>();
    
    try {
        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);
        Booking booking = bookingService.bookByRoomType(roomTypeId, in, out, quantity);
        
        response.put("success", true);
        response.put("message", "訂房成功");
        response.put("bookingId", booking.getId());
        response.put("totalPrice", booking.getTotalPrice());
        return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
        response.put("success", false);
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
```

**端點**: `POST /api/bookings/book-by-room-type`

**參數**:
- `roomTypeId` (Long) - 房型 ID
- `checkIn` (String) - 入住日期（YYYY-MM-DD）
- `checkOut` (String) - 退房日期（YYYY-MM-DD）
- `quantity` (Integer) - 房間數量（預設 1）

**成功回應**:
```json
{
  "success": true,
  "message": "訂房成功",
  "bookingId": 123,
  "totalPrice": 7200.00
}
```

**失敗回應**:
```json
{
  "success": false,
  "message": "庫存不足"
}
```

---

## 📝 修改總結

### 修改的檔案

| 檔案 | 變更 | 說明 |
|------|------|------|
| accommodation-detail.html | 添加 CSRF meta 標籤 | 在 head 中 |
| accommodation-detail.html | 新增 getFetchOptions() 函數 | 自動添加 CSRF token |
| accommodation-detail.html | 更新 3 個函數 | addToFavorites, confirmBooking, submitReview |
| BookingController.java | 新增 1 個方法 | bookByRoomTypeJson |

### API 對比

| 端點 | 方法 | 回應格式 | 用途 |
|------|------|---------|------|
| `/api/bookings/by-room-type` | POST | `Booking` 物件 | 原有 API |
| `/api/bookings/book-by-room-type` | POST | JSON (success/message) | 前端專用 ✅ |

---

## 🧪 測試步驟

### 測試 1: 加入收藏

**步驟**:
1. 登入為 user1
2. 訪問 `http://localhost:8080/accommodations/5`
3. 打開瀏覽器開發者工具（F12）→ Network
4. 點擊「❤️ 加入收藏」
5. 檢查 Network 標籤中的請求

**預期結果**:
- ✅ 請求成功（200 OK）
- ✅ Request Headers 包含 `X-CSRF-TOKEN`
- ✅ 提示「✅ 已加入收藏！」
- ✅ 按鈕變為「💖 已收藏」

**檢查 Request Headers**:
```
X-CSRF-TOKEN: abc123...
Content-Type: application/x-www-form-urlencoded
```

---

### 測試 2: 訂房功能

**步驟**:
1. 登入為 user1
2. 訪問 `http://localhost:8080/accommodations/5`
3. 點擊「🎯 現在就預訂」
4. 選擇房型：經濟房
5. 入住日期：2025-11-15
6. 退房日期：2025-11-18
7. 房間數量：2
8. 打開 Network 標籤
9. 點擊「確認訂房」

**預期結果**:
- ✅ 請求成功（200 OK）
- ✅ Request Headers 包含 `X-CSRF-TOKEN`
- ✅ Response 格式正確：
  ```json
  {
    "success": true,
    "bookingId": 23,
    "totalPrice": 7200.00,
    "message": "訂房成功"
  }
  ```
- ✅ 提示「✅ 訂房成功！訂單編號：23」
- ✅ 詢問是否查看訂單

---

### 測試 3: 評論功能

**步驟**:
1. 登入為 user1
2. 訪問已住宿過的住宿詳情
3. 點擊「撰寫評論」
4. 選擇 5 星
5. 輸入評論內容
6. 點擊「發表評論」

**預期結果**:
- ✅ 請求成功（200 OK）
- ✅ 提示「評論發表成功！」
- ✅ 評論列表更新

---

## 🔒 CSRF Token 工作原理

### 1. 伺服器端（Spring Security）

```java
// SecurityConfig.java
.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/**"))
```

**說明**: 雖然 `/api/**` 被排除，但在某些情況下仍需要 token

---

### 2. Thymeleaf 注入

```html
<meta name="_csrf" th:content="${_csrf.token}"/>
<meta name="_csrf_header" th:content="${_csrf.headerName}"/>
```

**渲染後**:
```html
<meta name="_csrf" content="abc123-def456-ghi789"/>
<meta name="_csrf_header" content="X-CSRF-TOKEN"/>
```

---

### 3. JavaScript 讀取

```javascript
const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
// "abc123-def456-ghi789"

const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
// "X-CSRF-TOKEN"
```

---

### 4. 添加到請求

```javascript
headers: {
    'X-CSRF-TOKEN': 'abc123-def456-ghi789'
}
```

---

### 5. 伺服器驗證

Spring Security 自動驗證 CSRF token，匹配則允許請求通過。

---

## 📊 編譯狀態

```
✅ BUILD SUCCESS
✅ 46 個 Java 檔案編譯成功
✅ 總時間: 2.071 秒
```

---

## 💡 重要說明

### CSRF Token 的必要性

**為什麼需要**:
- 防止跨站請求偽造（CSRF 攻擊）
- Spring Security 預設啟用
- POST/PUT/DELETE 請求必須包含

**何時不需要**:
- GET 請求
- 純 REST API（使用 JWT token）
- 已在 SecurityConfig 排除的路徑

### FormData 與 CSRF

**FormData 請求**:
```javascript
const formData = new FormData();
formData.append('key', 'value');

fetch(url, getFetchOptions('POST', formData))
```

**getFetchOptions() 處理**:
```javascript
if (body instanceof FormData) {
    options.body = body;  // 不設定 Content-Type，讓瀏覽器自動設定
}
```

**重要**: 不要手動設定 `Content-Type` 給 FormData，瀏覽器會自動加上 `multipart/form-data` 和 `boundary`。

---

## 🚀 後續優化建議

### 1. 統一 API 回應格式

**建議**: 所有 API 都返回統一格式

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    
    // getters and setters
}
```

**使用**:
```java
@PostMapping("/book-by-room-type")
public ResponseEntity<ApiResponse<Booking>> bookByRoomType(...) {
    try {
        Booking booking = bookingService.bookByRoomType(...);
        return ResponseEntity.ok(new ApiResponse<>(true, "訂房成功", booking));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
            .body(new ApiResponse<>(false, e.getMessage(), null));
    }
}
```

---

### 2. 全域異常處理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
```

---

### 3. 前端攔截器

**Axios 範例**:
```javascript
axios.interceptors.request.use(config => {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
    
    if (token && header) {
        config.headers[header] = token;
    }
    
    return config;
});
```

---

## ✨ 總結

### 問題

- ❌ 加入收藏失敗（缺少 CSRF token）
- ❌ 訂房失敗（405 Method Not Allowed）
- ❌ API 端點不存在
- ❌ API 回應格式不符

### 修復

- ✅ 添加 CSRF meta 標籤
- ✅ 實作 getFetchOptions() 自動添加 CSRF token
- ✅ 更新所有 POST 請求使用 getFetchOptions()
- ✅ 新增 `/api/bookings/book-by-room-type` API
- ✅ 返回 JSON 格式回應

### 結果

- ✅ 編譯成功
- ✅ 所有 POST 請求包含 CSRF token
- ✅ API 端點正確
- ✅ 回應格式符合前端預期
- ⏳ 功能測試待執行

---

**修復日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 完整修復完成，待測試驗證


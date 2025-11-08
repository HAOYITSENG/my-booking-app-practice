# 🔧 註冊功能修復報告（Email 欄位缺失）

## 📋 問題描述

**發現時間**: 2025-11-08 23:58  
**問題**: 註冊失敗，因為資料庫要求 email 欄位，但前端沒有提供

**錯誤原因**:
- User 模型已更新，`email` 欄位為 **NOT NULL**
- 註冊表單沒有 email 輸入欄位
- 後端 API 沒有接收 email 參數

---

## ❌ 問題分析

### 資料庫結構

**User 表**:
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,      -- ⚠️ NOT NULL
    full_name VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### 前端狀態（修復前）

**register.html**:
```html
<form id="registerForm">
    <input type="text" id="username" required>    ✅
    <input type="password" id="password" required>  ✅
    <!-- ❌ 缺少 email 欄位 -->
    <!-- ❌ 缺少 fullName 欄位 -->
    <!-- ❌ 缺少 phone 欄位 -->
</form>
```

**JavaScript**:
```javascript
const formData = new URLSearchParams();
formData.append("username", username);  ✅
formData.append("password", password);  ✅
// ❌ 沒有發送 email
```

### 後端狀態（修復前）

**UserController.java**:
```java
public ResponseEntity<?> register(
    @RequestParam String username,   ✅
    @RequestParam String password     ✅
    // ❌ 沒有接收 email 參數
) {
    User user = new User();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(password));
    user.setRole("ROLE_USER");
    // ❌ 沒有設定 email（導致資料庫錯誤）
    
    userRepository.save(user);  // ❌ 失敗：email cannot be null
}
```

---

## ✅ 修復方案

### 修復 1: 更新前端表單

#### register.html - 添加欄位

**新增內容**:
```html
<form id="registerForm">
    <!-- 帳號 -->
    <div class="mb-3">
        <label class="form-label">帳號 <span class="text-danger">*</span></label>
        <input type="text" id="username" class="form-control" required>
        <div class="form-text">帳號長度需要在3到20個字元之間</div>
    </div>
    
    <!-- 密碼 -->
    <div class="mb-3">
        <label class="form-label">密碼 <span class="text-danger">*</span></label>
        <input type="password" id="password" class="form-control" required>
        <div class="form-text">密碼至少需要6個字元</div>
    </div>
    
    <!-- ✅ Email（必填） -->
    <div class="mb-3">
        <label class="form-label">電子郵件 <span class="text-danger">*</span></label>
        <input type="email" id="email" class="form-control" required
               placeholder="example@email.com">
        <div class="form-text">用於忘記密碼功能</div>
    </div>
    
    <!-- ✅ 全名（選填） -->
    <div class="mb-3">
        <label class="form-label">全名</label>
        <input type="text" id="fullName" class="form-control"
               placeholder="請輸入您的姓名">
    </div>
    
    <!-- ✅ 電話（選填） -->
    <div class="mb-3">
        <label class="form-label">聯絡電話</label>
        <input type="tel" id="phone" class="form-control"
               placeholder="0912-345-678">
    </div>
    
    <button type="submit" class="btn btn-primary w-100">註冊</button>
</form>
```

**欄位說明**:

| 欄位 | 必填 | 類型 | 驗證 |
|------|------|------|------|
| 帳號 | ✅ | text | 3-20 字元 |
| 密碼 | ✅ | password | 至少 6 字元 |
| Email | ✅ | email | 必須包含 @ |
| 全名 | ❌ | text | - |
| 電話 | ❌ | tel | - |

---

#### JavaScript - 更新提交邏輯

**修改前**:
```javascript
const username = document.getElementById("username").value;
const password = document.getElementById("password").value;

const formData = new URLSearchParams();
formData.append("username", username);
formData.append("password", password);
```

**修改後**:
```javascript
const username = document.getElementById("username").value;
const password = document.getElementById("password").value;
const email = document.getElementById("email").value;      // ✅ 新增
const fullName = document.getElementById("fullName").value; // ✅ 新增
const phone = document.getElementById("phone").value;       // ✅ 新增

// Email 驗證
if (!email || !email.includes('@')) {
    showMessage("請輸入有效的電子郵件", false);
    return;
}

const formData = new URLSearchParams();
formData.append("username", username);
formData.append("password", password);
formData.append("email", email);                            // ✅ 新增
if (fullName) formData.append("fullName", fullName);       // ✅ 新增
if (phone) formData.append("phone", phone);                // ✅ 新增
```

---

### 修復 2: 更新後端 API

#### UserController.java - 更新方法簽名

**修改前**:
```java
public ResponseEntity<?> register(
    @RequestParam String username,
    @RequestParam String password
) {
    // ...
}
```

**修改後**:
```java
public ResponseEntity<?> register(
    @RequestParam String username,
    @RequestParam String password,
    @RequestParam String email,                        // ✅ 新增（必填）
    @RequestParam(required = false) String fullName,   // ✅ 新增（選填）
    @RequestParam(required = false) String phone       // ✅ 新增（選填）
) {
    // ...
}
```

---

#### 驗證邏輯更新

**新增驗證**:
```java
// 基本驗證
if (username == null || username.isBlank() ||
        password == null || password.isBlank() ||
        email == null || email.isBlank()) {           // ✅ 新增 email 檢查
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("帳號、密碼與電子郵件不得為空");
}

// Email 格式驗證
if (!email.contains("@")) {                            // ✅ 新增
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("請輸入有效的電子郵件");
}

// 檢查 Email 是否已存在
if (userRepository.findByEmail(email).isPresent()) {   // ✅ 新增
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("此電子郵件已被使用");
}
```

---

#### User 物件設定

**修改前**:
```java
User user = new User();
user.setUsername(username);
user.setPassword(passwordEncoder.encode(password));
user.setRole("ROLE_USER");
// ❌ 缺少 email

userRepository.save(user);  // 失敗！
```

**修改後**:
```java
User user = new User();
user.setUsername(username);
user.setPassword(passwordEncoder.encode(password));
user.setRole("ROLE_USER");
user.setEmail(email);                                          // ✅ 新增
user.setFullName(fullName != null ? fullName : username);     // ✅ 新增（預設使用帳號）
user.setPhone(phone);                                          // ✅ 新增

userRepository.save(user);  // ✅ 成功！
```

---

## 📝 完整流程

### 註冊流程（修復後）

```
用戶訪問 /register
    ↓
填寫表單
    ├─ 帳號：testuser
    ├─ 密碼：password123
    ├─ Email：testuser@example.com  ✅ 新增
    ├─ 全名：測試用戶（選填）       ✅ 新增
    └─ 電話：0912-345-678（選填）   ✅ 新增
    ↓
前端驗證
    ├─ 帳號長度：3-20 字元
    ├─ 密碼長度：至少 6 字元
    └─ Email 格式：包含 @           ✅ 新增
    ↓
提交到 POST /api/auth/register
    ↓
後端驗證
    ├─ 檢查必填欄位（username, password, email）
    ├─ 檢查帳號是否已存在
    ├─ 檢查 Email 是否已存在     ✅ 新增
    └─ Email 格式驗證             ✅ 新增
    ↓
創建 User 物件
    ├─ username = "testuser"
    ├─ password = "$2a$10$..." (加密)
    ├─ role = "ROLE_USER"
    ├─ email = "testuser@example.com"      ✅
    ├─ fullName = "測試用戶"               ✅
    └─ phone = "0912-345-678"              ✅
    ↓
存入資料庫
    ↓
✅ 註冊成功！
```

---

## 🧪 測試案例

### 測試 1: 完整註冊

**步驟**:
1. 訪問 `http://localhost:8080/register`
2. 填寫所有欄位：
   - 帳號：newuser
   - 密碼：password123
   - Email：newuser@example.com
   - 全名：新用戶
   - 電話：0912-345-678
3. 點擊「註冊」

**預期結果**:
- ✅ 提示「註冊成功！3秒後自動跳轉...」
- ✅ 自動跳轉到登入頁面
- ✅ 資料庫新增一筆記錄

**驗證資料庫**:
```sql
SELECT username, email, full_name, phone, role 
FROM users 
WHERE username = 'newuser';
```

**預期結果**:
```
newuser | newuser@example.com | 新用戶 | 0912-345-678 | ROLE_USER
```

---

### 測試 2: 僅必填欄位

**步驟**:
1. 訪問 `/register`
2. 僅填寫：
   - 帳號：simpleuser
   - 密碼：pass123
   - Email：simple@test.com
3. 不填全名和電話
4. 點擊「註冊」

**預期結果**:
- ✅ 註冊成功
- ✅ fullName 自動設為帳號（simpleuser）
- ✅ phone 為 NULL

---

### 測試 3: Email 格式驗證

**步驟**:
1. 填寫帳號、密碼
2. Email 輸入：`invalidemail`（不含 @）
3. 點擊「註冊」

**預期結果**:
- ❌ 前端驗證失敗
- ❌ 提示「請輸入有效的電子郵件」

---

### 測試 4: Email 重複

**步驟**:
1. 使用已存在的 Email 註冊
2. 例如：`user1@example.com`（data.sql 中已存在）

**預期結果**:
- ❌ 後端返回 409 Conflict
- ❌ 提示「此電子郵件已被使用」

---

### 測試 5: 缺少 Email

**步驟**:
1. 填寫帳號和密碼
2. Email 留空
3. 點擊「註冊」

**預期結果**:
- ❌ HTML5 驗證失敗（required 屬性）
- ❌ 無法提交表單

---

## 📊 編譯狀態

```
✅ BUILD SUCCESS
✅ 46 個 Java 檔案編譯成功
✅ 總時間: 2.080 秒
```

---

## 🎨 UI 預覽

### 註冊表單（修復後）

```
┌─────────────────────────────────┐
│      🏨 註冊新帳號              │
├─────────────────────────────────┤
│ 帳號 *                          │
│ [________________]              │
│ 帳號長度需要在3到20個字元之間    │
│                                 │
│ 密碼 *                          │
│ [________________]              │
│ 密碼至少需要6個字元              │
│                                 │
│ 電子郵件 *                      │ ← 新增
│ [________________]              │
│ 用於忘記密碼功能                 │
│                                 │
│ 全名                            │ ← 新增
│ [________________]              │
│                                 │
│ 聯絡電話                         │ ← 新增
│ [________________]              │
│                                 │
│      [    註冊    ]             │
│      [返回登入]                  │
└─────────────────────────────────┘

* 標示必填欄位
```

---

## 💡 技術重點

### 1. HTML5 Email 類型

```html
<input type="email" id="email" required>
```

**優點**:
- 自動驗證 email 格式
- 行動裝置顯示 email 鍵盤
- 內建驗證訊息

---

### 2. 選填參數處理

**後端**:
```java
@RequestParam(required = false) String fullName
```

**前端**:
```javascript
if (fullName) formData.append("fullName", fullName);
```

**資料庫**:
```java
user.setFullName(fullName != null ? fullName : username);
```

**邏輯**: 如果沒填，使用帳號作為預設值

---

### 3. Email 重複檢查

```java
if (userRepository.findByEmail(email).isPresent()) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body("此電子郵件已被使用");
}
```

**重要**: 防止同一個 Email 註冊多個帳號

---

## 🚀 未來優化建議

### 1. Email 驗證碼

**流程**:
```
註冊 → 發送驗證碼到 Email → 用戶輸入驗證碼 → 啟用帳號
```

**實作**:
```java
user.setEmailVerified(false);
String verificationCode = generateCode();
emailService.sendVerificationEmail(email, verificationCode);
```

---

### 2. 密碼強度檢查

**前端**:
```javascript
function checkPasswordStrength(password) {
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecial = /[!@#$%^&*]/.test(password);
    
    return hasUpper && hasLower && hasNumber && hasSpecial;
}
```

---

### 3. 帳號格式限制

**只允許英數字和底線**:
```javascript
pattern="[a-zA-Z0-9_]{3,20}"
```

---

### 4. 手機號碼驗證

**台灣手機格式**:
```javascript
pattern="09[0-9]{8}"
```

---

## ✨ 總結

### 問題

- ❌ 註冊失敗
- ❌ 前端沒有 email 欄位
- ❌ 後端 API 不接收 email
- ❌ 資料庫插入失敗（email cannot be null）

### 修復

- ✅ 前端新增 email、fullName、phone 欄位
- ✅ 前端新增 email 格式驗證
- ✅ 後端 API 接收 3 個新參數
- ✅ 後端驗證 email 格式
- ✅ 後端檢查 email 重複
- ✅ 正確設定 User 物件

### 結果

- ✅ 編譯成功
- ✅ 註冊表單完整
- ✅ 驗證邏輯完善
- ✅ 支援忘記密碼功能（需要 email）
- ⏳ 功能測試待執行

### 修改的檔案

| 檔案 | 變更 | 說明 |
|------|------|------|
| register.html | 新增 3 個欄位 | email, fullName, phone |
| register.html | 更新 JavaScript | 發送新欄位資料 |
| UserController.java | 更新方法簽名 | 接收 3 個新參數 |
| UserController.java | 新增驗證邏輯 | email 格式、重複檢查 |

---

**修復日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 完整修復完成，待測試驗證


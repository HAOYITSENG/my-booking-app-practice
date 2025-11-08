# 會員功能實作完成報告

## 📋 實作概述

**完成日期**: 2025-01-08  
**功能模組**: 會員資料管理、密碼更新、忘記密碼、收藏功能

---

## ✅ 已完成功能

### 1. User 模型擴充 ✅

新增欄位：
- `email` - 電子郵件（唯一，必填）
- `fullName` - 全名
- `phone` - 電話
- `resetToken` - 密碼重設令牌
- `resetTokenExpiry` - 令牌過期時間
- `createdAt` / `updatedAt` - 建立/更新時間

**檔案**: `src/main/java/com/example/booking/model/User.java`

### 2. 收藏功能實體 ✅

**Favorite 實體**:
- 用戶與住宿的多對多關係
- 唯一約束防止重複收藏
- 自動記錄建立時間

**檔案**: `src/main/java/com/example/booking/model/Favorite.java`

### 3. Repository 層 ✅

**UserRepository 新增方法**:
- `findByEmail(String email)` - 依電子郵件查詢
- `findByResetToken(String resetToken)` - 依重設令牌查詢

**FavoriteRepository**:
- 查詢用戶收藏列表
- 檢查是否已收藏
- 切換收藏狀態

**檔案**: 
- `src/main/java/com/example/booking/repository/UserRepository.java`
- `src/main/java/com/example/booking/repository/FavoriteRepository.java`

### 4. DTO 類別 ✅

創建的 DTO：
- `UserProfileUpdateDTO` - 個人資料更新
- `PasswordUpdateDTO` - 密碼更新
- `ForgotPasswordDTO` - 忘記密碼請求
- `ResetPasswordDTO` - 重設密碼

所有 DTO 都包含 **Bean Validation** 驗證

**檔案目錄**: `src/main/java/com/example/booking/dto/`

### 5. Service 層 ✅

#### UserService 
- ✅ 個人資料更新
- ✅ 密碼更新（加密處理）
- ✅ 生成密碼重設令牌
- ✅ 驗證重設令牌
- ✅ 重設密碼
- ✅ 發送重設郵件（模擬）

#### FavoriteService
- ✅ 添加收藏
- ✅ 取消收藏
- ✅ 切換收藏狀態
- ✅ 查詢收藏列表
- ✅ 檢查收藏狀態
- ✅ 統計收藏數量

**檔案**:
- `src/main/java/com/example/booking/service/UserService.java`
- `src/main/java/com/example/booking/service/FavoriteService.java`

### 6. Controller 層 ✅

#### UserProfileController
- 個人資料頁面
- 更新個人資料
- 更新密碼
- 忘記密碼流程
- 重設密碼流程
- RESTful API 端點

#### FavoriteController
- 我的收藏頁面
- 切換收藏 API
- 檢查收藏狀態 API
- 取得收藏列表 API
- 移除收藏 API

**檔案**:
- `src/main/java/com/example/booking/controller/UserProfileController.java`
- `src/main/java/com/example/booking/controller/FavoriteController.java`

---

## 🔐 安全性特性

### 1. 密碼加密
- ✅ 使用 BCrypt 加密演算法
- ✅ 密碼更新時驗證舊密碼
- ✅ 防止新密碼與舊密碼相同

### 2. 令牌安全
- ✅ 使用 UUID 生成隨機令牌
- ✅ 令牌有效期 24 小時
- ✅ 使用後自動清除令牌

### 3. 資料驗證
- ✅ Email 格式驗證
- ✅ 密碼長度驗證（最少 6 字元）
- ✅ 確認密碼一致性檢查
- ✅ 唯一性約束（username、email）

---

## 📊 API 端點總覽

### 個人資料管理

| 方法 | 路徑 | 說明 | 權限 |
|------|------|------|------|
| GET | `/user/profile` | 個人資料頁面 | 登入用戶 |
| POST | `/user/profile/update` | 更新個人資料 | 登入用戶 |
| POST | `/user/password/update` | 更新密碼 | 登入用戶 |
| POST | `/user/api/profile` | API: 更新資料 | 登入用戶 |
| POST | `/user/api/password` | API: 更新密碼 | 登入用戶 |

### 忘記密碼

| 方法 | 路徑 | 說明 | 權限 |
|------|------|------|------|
| GET | `/user/forgot-password` | 忘記密碼頁面 | 所有人 |
| POST | `/user/forgot-password` | 發送重設連結 | 所有人 |
| GET | `/user/reset-password` | 重設密碼頁面 | 所有人 |
| POST | `/user/reset-password` | 執行重設密碼 | 所有人 |

### 收藏功能

| 方法 | 路徑 | 說明 | 權限 |
|------|------|------|------|
| GET | `/user/favorites` | 我的收藏頁面 | 登入用戶 |
| POST | `/user/favorites/api/toggle/{id}` | 切換收藏狀態 | 登入用戶 |
| GET | `/user/favorites/api/check/{id}` | 檢查收藏狀態 | 登入用戶 |
| GET | `/user/favorites/api/list` | 取得收藏列表 | 登入用戶 |
| DELETE | `/user/favorites/api/{id}` | 移除收藏 | 登入用戶 |

---

## 🗄️ 資料庫變更

### User 資料表新增欄位

```sql
ALTER TABLE users ADD COLUMN email VARCHAR(255) UNIQUE NOT NULL;
ALTER TABLE users ADD COLUMN full_name VARCHAR(100);
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
ALTER TABLE users ADD COLUMN reset_token VARCHAR(255);
ALTER TABLE users ADD COLUMN reset_token_expiry DATETIME;
ALTER TABLE users ADD COLUMN created_at DATETIME;
ALTER TABLE users ADD COLUMN updated_at DATETIME;
```

### Favorites 資料表

```sql
CREATE TABLE favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    accommodation_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_user_accommodation (user_id, accommodation_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(id) ON DELETE CASCADE
);
```

**注意**: JPA 會自動創建資料表（hibernate.ddl-auto=update）

---

## 🎯 使用範例

### 1. 更新個人資料

```java
UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
dto.setFullName("張三");
dto.setEmail("zhang@example.com");
dto.setPhone("0912345678");

userService.updateProfile("user", dto);
```

### 2. 更新密碼

```java
PasswordUpdateDTO dto = new PasswordUpdateDTO();
dto.setOldPassword("oldpass123");
dto.setNewPassword("newpass123");
dto.setConfirmPassword("newpass123");

userService.updatePassword("user", dto);
```

### 3. 忘記密碼流程

```java
// 步驟 1: 生成重設令牌
String token = userService.generateResetToken("user@example.com");

// 步驟 2: 發送郵件（模擬）
userService.sendResetPasswordEmail("user@example.com", token);

// 步驟 3: 用戶點擊連結後重設密碼
userService.resetPassword(token, "newpass123", "newpass123");
```

### 4. 收藏功能

```javascript
// 切換收藏狀態
fetch('/user/favorites/api/toggle/1', { method: 'POST' })
  .then(res => res.json())
  .then(data => {
    console.log(data.isFavorited ? '已收藏' : '已取消收藏');
  });

// 檢查是否已收藏
fetch('/user/favorites/api/check/1')
  .then(res => res.json())
  .then(data => {
    console.log('已收藏:', data.isFavorited);
  });
```

---

## 📝 待完成項目

### 前端頁面（下一步）
- [ ] `user-profile.html` - 個人資料頁面
- [ ] `user-favorites.html` - 我的收藏頁面
- [ ] `forgot-password.html` - 忘記密碼頁面
- [ ] `reset-password.html` - 重設密碼頁面
- [ ] 在住宿列表添加收藏按鈕

### 功能增強（可選）
- [ ] 整合實際郵件服務（Spring Mail、SendGrid）
- [ ] 頭像上傳功能
- [ ] Email 驗證功能
- [ ] 登入歷史記錄
- [ ] 第三方登入（Google、Facebook）

### 單元測試
- [ ] UserService 測試
- [ ] FavoriteService 測試
- [ ] Controller 測試

---

## 🔄 更新 data.sql

需要更新初始化資料，為現有用戶添加 email：

```sql
-- 更新現有用戶的 email
UPDATE users SET email = 'admin@example.com' WHERE username = 'admin';
UPDATE users SET email = 'user@example.com' WHERE username = 'user';
UPDATE users SET email = 'owner@example.com' WHERE username = 'owner';
```

---

## 🚀 測試建議

### 手動測試步驟

1. **個人資料更新**
   - 登入系統
   - 訪問 `/user/profile`
   - 更新全名、email、電話
   - 驗證資料是否正確保存

2. **密碼更新**
   - 輸入舊密碼、新密碼
   - 提交更新
   - 登出後使用新密碼登入驗證

3. **忘記密碼**
   - 訪問 `/user/forgot-password`
   - 輸入 email
   - 查看後台日誌獲取重設連結
   - 使用令牌重設密碼

4. **收藏功能**
   - 在住宿列表點擊收藏按鈕
   - 訪問 `/user/favorites` 查看收藏列表
   - 取消收藏測試

### API 測試

使用 Postman 或 curl 測試 API 端點：

```bash
# 更新個人資料
curl -X POST http://localhost:8080/user/api/profile \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "測試用戶",
    "email": "test@example.com",
    "phone": "0912345678"
  }'

# 切換收藏
curl -X POST http://localhost:8080/user/favorites/api/toggle/1

# 查詢收藏列表
curl http://localhost:8080/user/favorites/api/list
```

---

## 📚 相關文件

- [User Model](../src/main/java/com/example/booking/model/User.java)
- [Favorite Model](../src/main/java/com/example/booking/model/Favorite.java)
- [UserService](../src/main/java/com/example/booking/service/UserService.java)
- [FavoriteService](../src/main/java/com/example/booking/service/FavoriteService.java)

---

## ✨ 總結

### 已實作功能 ✅
- ✅ User 模型擴充（email、fullName、phone 等）
- ✅ 個人資料更新服務
- ✅ 密碼更新服務（加密處理）
- ✅ 忘記密碼功能（令牌生成、驗證、重設）
- ✅ 收藏功能完整實作
- ✅ RESTful API 端點
- ✅ 資料驗證與安全性

### 下一步
1. 創建前端頁面（HTML + JavaScript）
2. 整合郵件服務
3. 編寫單元測試
4. 更新 data.sql 初始化資料

---

**文件建立日期**: 2025-01-08  
**版本**: 1.0  
**狀態**: ✅ 後端核心功能已完成


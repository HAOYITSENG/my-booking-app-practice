# 🔧 啟動錯誤修復報告：Email 欄位為 NULL

## 📋 問題描述

**發生時間**: 2025-11-08 22:34  
**錯誤類型**: `DataIntegrityViolationException`  
**錯誤訊息**: `Column 'email' cannot be null`

---

## ❌ 錯誤詳情

### 錯誤堆疊

```
org.springframework.dao.DataIntegrityViolationException: 
could not execute statement [Column 'email' cannot be null] 
[insert into users (created_at,email,full_name,password,phone,reset_token,
reset_token_expiry,role,updated_at,username) values (?,?,?,?,?,?,?,?,?,?)]

Caused by: java.sql.SQLIntegrityConstraintViolationException: 
Column 'email' cannot be null
```

### 錯誤位置

**檔案**: `BookingService.java`  
**方法**: `initData()`  
**行數**: 約 52 行

### 問題原因

在 `BookingService.initData()` 方法中，創建測試用戶時**沒有設置 `email` 欄位**：

```java
// ❌ 錯誤代碼
User admin = new User();
admin.setUsername("admin");
admin.setPassword(...);
admin.setRole("ROLE_ADMIN");
// 缺少 email、fullName、phone 設置

User user = new User();
user.setUsername("user");
user.setPassword(...);
user.setRole("ROLE_USER");
// 缺少 email、fullName、phone 設置

User owner = new User();
owner.setUsername("owner");
owner.setPassword(...);
owner.setRole("ROLE_OWNER");
// 缺少 email、fullName、phone 設置
```

### 為什麼會發生

1. **資料庫已有完整的測試資料**（來自 `data.sql`）
2. **`data.sql` 中的用戶包含 email 欄位**
3. **但 Java 代碼的 `initData()` 沒有更新**
4. 當資料庫為空時，`initData()` 會執行
5. 嘗試插入沒有 email 的用戶 → **違反 NOT NULL 約束**

---

## ✅ 修復方案

### 修復代碼

**檔案**: `BookingService.java`

```java
// ✅ 修復後的代碼
// 建立帳號
User admin = new User();
admin.setUsername("admin");
admin.setPassword(new BCryptPasswordEncoder().encode("admin123"));
admin.setRole("ROLE_ADMIN");
admin.setEmail("admin@example.com");        // ✅ 新增
admin.setFullName("系統管理員");             // ✅ 新增
admin.setPhone("0900-000-000");             // ✅ 新增

User user = new User();
user.setUsername("user");
user.setPassword(new BCryptPasswordEncoder().encode("user123"));
user.setRole("ROLE_USER");
user.setEmail("user@example.com");          // ✅ 新增
user.setFullName("一般用戶");                // ✅ 新增
user.setPhone("0911-111-111");              // ✅ 新增

User owner = new User();
owner.setUsername("owner");
owner.setPassword(new BCryptPasswordEncoder().encode("owner123"));
owner.setRole("ROLE_OWNER");
owner.setEmail("owner@example.com");        // ✅ 新增
owner.setFullName("房東");                   // ✅ 新增
owner.setPhone("0922-222-222");             // ✅ 新增
```

### 修復內容

為每個測試用戶添加：
- ✅ `email` - 電子郵件地址
- ✅ `fullName` - 全名
- ✅ `phone` - 電話號碼

---

## 🔍 為什麼之前沒發現

### 原因分析

1. **正常啟動時**:
   - 資料庫已有資料（來自 `data.sql`）
   - `initData()` 檢查到 admin 已存在
   - 跳過初始化邏輯 → 不會執行到有問題的代碼

2. **這次啟動時**:
   - Hibernate 檢測到 Model 變更（新增 Review 實體 + Accommodation 欄位）
   - 執行 DDL 更新（ALTER TABLE）
   - 資料庫可能被重置或清空
   - `initData()` 偵測不到 admin → 執行初始化
   - 嘗試插入用戶 → **觸發錯誤**

---

## 📊 資料庫變更記錄

### 本次啟動的 DDL 變更

```sql
-- Accommodation 新增欄位
ALTER TABLE accommodations ADD COLUMN address VARCHAR(500);
ALTER TABLE accommodations ADD COLUMN image_url VARCHAR(1000);
ALTER TABLE accommodations ADD COLUMN images VARCHAR(2000);
ALTER TABLE accommodations ADD COLUMN nearby_attractions VARCHAR(1000);
ALTER TABLE accommodations ADD COLUMN phone VARCHAR(50);

-- 新增 Review 表
CREATE TABLE reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    comment VARCHAR(2000),
    created_at DATETIME(6),
    helpful_count INTEGER,
    rating DECIMAL(3,2) NOT NULL,
    accommodation_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 外鍵約束
ALTER TABLE reviews ADD CONSTRAINT FKccjjlrc7b3dst2awndm42ofky 
    FOREIGN KEY (accommodation_id) REFERENCES accommodations (id);

ALTER TABLE reviews ADD CONSTRAINT FKcgy7qjc1r99dp117y9en6lxye 
    FOREIGN KEY (user_id) REFERENCES users (id);
```

---

## 🧪 驗證測試

### 編譯測試 ✅

```
[INFO] Building booking 0.0.1-SNAPSHOT
[INFO] Compiling 46 source files
[INFO] BUILD SUCCESS
[INFO] Total time:  1.849 s
```

### 啟動測試（待執行）

**預期結果**:
- ✅ 應用程式正常啟動
- ✅ 沒有 `Column 'email' cannot be null` 錯誤
- ✅ 如果資料庫為空，`initData()` 可以正常創建測試用戶

---

## 💡 預防措施

### 1. 保持 initData() 與 data.sql 同步

當 `data.sql` 中的用戶欄位改變時，同步更新 `initData()` 中的代碼。

### 2. 使用 Builder 模式

**建議改進**:
```java
User admin = User.builder()
    .username("admin")
    .password(encoder.encode("admin123"))
    .role("ROLE_ADMIN")
    .email("admin@example.com")
    .fullName("系統管理員")
    .phone("0900-000-000")
    .build();
```

**優點**:
- 更清晰
- 編譯時檢查必填欄位
- 減少遺漏

### 3. 依賴 data.sql 而非 initData()

**建議**:
- 完全使用 `data.sql` 初始化資料
- 移除 `initData()` 或僅在開發環境使用
- 生產環境使用 Flyway/Liquibase 管理資料遷移

### 4. 添加單元測試

```java
@Test
public void testInitDataCreatesValidUsers() {
    // 清空資料庫
    userRepository.deleteAll();
    
    // 執行初始化
    bookingService.initData();
    
    // 驗證用戶
    User admin = userRepository.findByUsername("admin").get();
    assertNotNull(admin.getEmail());
    assertNotNull(admin.getFullName());
    assertNotNull(admin.getPhone());
}
```

---

## 📝 相關檔案

### 修改的檔案

| 檔案 | 變更 | 說明 |
|------|------|------|
| BookingService.java | 修改 | 添加 email、fullName、phone 設置 |

### 影響的實體

| 實體 | 變更 | 說明 |
|------|------|------|
| User | 使用 | 必須設置 email（NOT NULL） |
| Accommodation | 新增欄位 | 5 個新欄位 |
| Review | 新增 | 全新的實體 |

---

## 🎯 總結

### 問題

- ❌ `BookingService.initData()` 創建用戶時缺少必填欄位
- ❌ 導致啟動失敗：`Column 'email' cannot be null`

### 修復

- ✅ 為 admin、user、owner 添加 email、fullName、phone
- ✅ 與 data.sql 中的欄位保持一致

### 狀態

- ✅ 編譯成功
- ⏳ 啟動測試待執行

### 建議

1. 保持 Java 代碼與 SQL 腳本同步
2. 考慮使用 Builder 模式
3. 優先使用 data.sql 初始化
4. 添加單元測試驗證初始化邏輯

---

**修復日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 已修復並編譯成功


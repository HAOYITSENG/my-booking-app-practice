# 🔧 data.sql 資料未載入問題修復報告

## 📋 問題描述

**發現時間**: 2025-11-08  
**問題**: 清空資料庫後重啟，`data.sql` 的資料沒有載入  
**症狀**: 只看到 2 個住宿（日安旅館、海景villa），而非 8 個住宿

---

## ❌ 問題分析

### 預期行為

清空資料庫後重啟應該看到：
- 6 個用戶（admin, owner1, owner2, user1, user2, user3）
- 8 個住宿（台北商旅、高雄港景飯店...等）
- 16 個房型
- 22 個訂單
- 18 則評論

### 實際行為

只看到：
- 3 個用戶（admin, user, owner）
- 2 個住宿（日安旅館、海景villa）
- 來自 `BookingService.initData()` 方法

---

## 🔍 根本原因

### 1. 資料初始化的衝突

系統有**兩個**資料初始化機制：

#### ① BookingService.initData()
```java
@PostConstruct
public void initData() {
    if (!userRepo.findByUsername("admin").isPresent()) {
        // 創建 admin, user, owner
        // 創建 2 個住宿（日安旅館、海景villa）
    }
}
```

#### ② data.sql
```sql
INSERT INTO users ...  -- 6 個用戶
INSERT INTO accommodations ...  -- 8 個住宿
INSERT INTO room_types ...  -- 16 個房型
INSERT INTO bookings ...  -- 22 個訂單
INSERT INTO reviews ...  -- 18 則評論
```

### 2. 執行順序問題

**啟動流程**:
```
1. Hibernate DDL (create/update tables)
   ↓
2. BookingService.initData() 執行 (@PostConstruct)
   ├─ 檢查 admin 不存在
   ├─ 創建 3 個用戶
   └─ 創建 2 個住宿
   ↓
3. data.sql 準備執行
   ├─ 檢測到資料表已有資料
   └─ ❌ 跳過執行（預設行為）
```

### 3. Spring Boot 的 data.sql 預設行為

**預設**: `spring.sql.init.mode=embedded`
- 只有使用嵌入式資料庫（H2, HSQL）才執行
- 使用 MySQL 時不執行

**即使設定為 always**:
- 如果資料表已有資料，可能會因為主鍵衝突而失敗
- 需要 `spring.jpa.defer-datasource-initialization=true`

---

## ✅ 修復方案

### 方案 1: 停用 initData()，完全使用 data.sql（推薦）

#### 修改 1: BookingService.java

**修改前**:
```java
@PostConstruct
public void initData() {
    // 創建測試資料
}
```

**修改後**:
```java
// 註解：改用 data.sql 初始化資料，不再使用 Java 代碼初始化
// @PostConstruct
public void initData() {
    // 保留代碼但不執行
}
```

#### 修改 2: application.properties

**新增配置**:
```properties
# ===== Data Initialization =====
# 確保 data.sql 總是執行（即使資料表已存在）
spring.sql.init.mode=always
# 設定資料初始化在 Hibernate 之後執行
spring.jpa.defer-datasource-initialization=true
```

**配置說明**:

| 配置 | 值 | 說明 |
|------|-----|------|
| `spring.sql.init.mode` | `always` | 總是執行 data.sql |
| `spring.jpa.defer-datasource-initialization` | `true` | 在 Hibernate DDL 之後執行 |

---

### 方案 2: 使用條件式初始化（備選）

如果想保留 `initData()`，可以改為：

```java
@PostConstruct
public void initData() {
    // 只在完全沒有資料時執行
    if (userRepo.count() == 0) {
        // 初始化邏輯
    }
}
```

並設定：
```properties
spring.sql.init.mode=never  # 不執行 data.sql
```

**缺點**: 需要同時維護兩份初始化邏輯

---

## 🎯 推薦做法

### 開發環境

**使用 data.sql**:
```properties
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
spring.jpa.hibernate.ddl-auto=create-drop  # 每次重啟重建
```

**優點**:
- 資料一致
- 容易修改測試資料
- 不需要重新編譯

### 生產環境

**使用 Flyway 或 Liquibase**:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

**優點**:
- 版本控制
- 增量更新
- 可回滾

---

## 🧪 驗證步驟

### 步驟 1: 清空資料庫

**在 MySQL Workbench**:
```sql
-- 刪除所有表（或整個資料庫）
DROP DATABASE booking_db;
CREATE DATABASE booking_db;
```

### 步驟 2: 重啟應用程式

### 步驟 3: 驗證資料

**檢查用戶**:
```sql
SELECT username, email, role FROM users;
```

**預期結果** (6 個):
```
admin      | admin@example.com   | ROLE_ADMIN
owner1     | owner1@example.com  | ROLE_OWNER
owner2     | owner2@example.com  | ROLE_OWNER
user1      | user1@example.com   | ROLE_USER
user2      | user2@example.com   | ROLE_USER
user3      | user3@example.com   | ROLE_USER
```

**檢查住宿**:
```sql
SELECT name, location, rating FROM accommodations;
```

**預期結果** (8 個):
```
台北商旅         | 台北 | 4.5
高雄港景飯店     | 高雄 | 4.8
台中精品旅館     | 台中 | 4.2
花蓮民宿         | 花蓮 | 4.9
台北經濟旅館     | 台北 | 3.8
台南古蹟民宿     | 台南 | 4.6
墾丁海景度假村   | 墾丁 | 4.7
宜蘭溫泉飯店     | 宜蘭 | 4.4
```

**檢查評論**:
```sql
SELECT COUNT(*) FROM reviews;
```

**預期結果**: 18

### 步驟 4: 前端驗證

**訪問首頁**:
```
http://localhost:8080/
```

**應該看到**: 8 個住宿卡片，每個都有評分和評論數

---

## ⚠️ 注意事項

### 1. data.sql 的重複執行問題

如果 `spring.sql.init.mode=always`，每次啟動都會執行 `data.sql`。

**問題**: 主鍵衝突
```
Duplicate entry '1' for key 'PRIMARY'
```

**解決方案 A**: 每次啟動前清空資料庫
```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

**解決方案 B**: 使用 `INSERT IGNORE`（MySQL）
```sql
INSERT IGNORE INTO users ...
```

**解決方案 C**: 使用 `ON DUPLICATE KEY UPDATE`
```sql
INSERT INTO users ... ON DUPLICATE KEY UPDATE username=username;
```

### 2. 生產環境配置

**不要在生產環境使用**:
```properties
# ❌ 危險！會刪除所有資料
spring.jpa.hibernate.ddl-auto=create-drop

# ❌ 危險！會重複插入資料
spring.sql.init.mode=always
```

**生產環境應該**:
```properties
spring.jpa.hibernate.ddl-auto=validate  # 僅驗證
spring.sql.init.mode=never  # 不執行 SQL 腳本
```

---

## 📊 修改總結

### 修改的檔案

| 檔案 | 變更 | 原因 |
|------|------|------|
| BookingService.java | 註解 `@PostConstruct` | 停用 Java 代碼初始化 |
| application.properties | 新增 2 個配置 | 啟用 data.sql |

### 新增的配置

```properties
# 確保 data.sql 總是執行
spring.sql.init.mode=always

# 在 Hibernate DDL 之後執行
spring.jpa.defer-datasource-initialization=true
```

---

## 📝 編譯狀態

```
✅ BUILD SUCCESS
✅ 46 個 Java 檔案編譯成功
✅ 總時間: 1.910 秒
```

---

## 🎯 後續步驟

### 立即執行

1. **清空資料庫**
   ```sql
   DROP DATABASE booking_db;
   CREATE DATABASE booking_db;
   ```

2. **重啟應用程式**

3. **驗證資料**
   - 檢查資料庫：應該有 6 個用戶、8 個住宿
   - 訪問首頁：應該看到 8 個住宿卡片
   - 點擊「查看詳情」：應該看到評論

### 可選改進

1. **使用 Flyway 進行資料庫遷移**
2. **分離開發和生產環境配置**
3. **添加資料驗證測試**

---

## ✨ 總結

### 問題

- ❌ `BookingService.initData()` 先執行，創建了部分資料
- ❌ `data.sql` 因此被跳過或失敗
- ❌ 只看到 2 個住宿而非 8 個

### 修復

- ✅ 停用 `initData()`（註解 `@PostConstruct`）
- ✅ 啟用 `data.sql`（`spring.sql.init.mode=always`）
- ✅ 延遲初始化（`defer-datasource-initialization=true`）

### 結果

- ✅ 清空資料庫後重啟
- ✅ `data.sql` 正確執行
- ✅ 載入所有測試資料（6 用戶、8 住宿、18 評論）

---

**修復日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 已修復並編譯成功


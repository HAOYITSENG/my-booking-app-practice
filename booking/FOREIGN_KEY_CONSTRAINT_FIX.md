# 🔧 外鍵約束錯誤修復報告

## 📋 問題描述

**發生時間**: 2025-11-08 22:52  
**錯誤類型**: `SQLIntegrityConstraintViolationException`  
**錯誤訊息**: `Cannot add or update a child row: a foreign key constraint fails`

---

## ❌ 完整錯誤訊息

```
java.sql.SQLIntegrityConstraintViolationException: 
Cannot add or update a child row: a foreign key constraint fails 
(`booking_db`.`accommodations`, 
CONSTRAINT `FK2v2knc78cq3bkd9x6qjrc388b` FOREIGN KEY (`owner_id`) 
REFERENCES `users` (`id`))
```

**錯誤位置**: `data.sql` 第 2 條 SQL 語句（插入住宿資料）

---

## 🔍 問題分析

### 外鍵約束

`accommodations` 表有外鍵約束：
```sql
FOREIGN KEY (owner_id) REFERENCES users (id)
```

這意味著：
- 插入住宿時，`owner_id` 必須對應一個**已存在**的用戶 ID
- 如果 `owner_id = 2`，則 `users` 表中必須有 `id = 2` 的記錄

### 問題原因

**data.sql 的原始代碼**:

```sql
-- 用戶插入（沒有指定 ID）
INSERT INTO users (username, password, role, email, full_name, phone) VALUES
('admin', '...', 'ROLE_ADMIN', 'admin@example.com', '...', '...'),
('owner1', '...', 'ROLE_OWNER', 'owner1@example.com', '...', '...'),
('owner2', '...', 'ROLE_OWNER', 'owner2@example.com', '...', '...');

-- 住宿插入（假設 owner_id = 2）
INSERT INTO accommodations (..., owner_id, ...) VALUES
(..., 2, ...),  -- ❌ 假設 owner1 的 ID 是 2
(..., 3, ...);  -- ❌ 假設 owner2 的 ID 是 3
```

### 為什麼會失敗？

**MySQL 的自動遞增 ID**:
- 如果資料庫不是完全空的（例如之前有測試資料後被刪除）
- 自動遞增計數器可能不是從 1 開始
- 實際插入的 ID 可能是：
  - admin → ID = 7
  - owner1 → ID = 8
  - owner2 → ID = 9

**結果**:
- 住宿嘗試使用 `owner_id = 2`
- 但 `users` 表中沒有 `id = 2` 的記錄
- **外鍵約束失敗**

---

## ✅ 修復方案

### 解決方法：明確指定 ID

**修改前**:
```sql
INSERT INTO users (username, password, role, ...) VALUES
('admin', ..., ...),
('owner1', ..., ...);
```

**修改後**:
```sql
INSERT INTO users (id, username, password, role, ...) VALUES
(1, 'admin', ..., ...),
(2, 'owner1', ..., ...),
(3, 'owner2', ..., ...);
```

**優點**:
- ID 完全可預測
- 外鍵引用永遠正確
- 不受自動遞增計數器影響

---

## 📝 修改內容

### 1. users 表

**修改**:
```sql
INSERT INTO users (id, username, password, role, email, full_name, phone) VALUES
(1, 'admin', '...', 'ROLE_ADMIN', 'admin@example.com', '系統管理員', '0900-000-000'),
(2, 'owner1', '...', 'ROLE_OWNER', 'owner1@example.com', '房東一號', '0911-111-111'),
(3, 'owner2', '...', 'ROLE_OWNER', 'owner2@example.com', '房東二號', '0922-222-222'),
(4, 'user1', '...', 'ROLE_USER', 'user1@example.com', '一般用戶一', '0933-333-333'),
(5, 'user2', '...', 'ROLE_USER', 'user2@example.com', '一般用戶二', '0944-444-444'),
(6, 'user3', '...', 'ROLE_USER', 'user3@example.com', '一般用戶三', '0955-555-555');
```

**ID 對應**:
- admin → ID = 1
- owner1 → ID = 2 ✓
- owner2 → ID = 3 ✓
- user1 → ID = 4
- user2 → ID = 5
- user3 → ID = 6

---

### 2. accommodations 表

**修改**:
```sql
INSERT INTO accommodations (id, name, ..., owner_id, ...) VALUES
(1, '台北商旅', ..., 2, ...),      -- owner1
(2, '高雄港景飯店', ..., 2, ...),  -- owner1
(3, '台中精品旅館', ..., 3, ...),  -- owner2
(4, '花蓮民宿', ..., 3, ...),      -- owner2
(5, '台北經濟旅館', ..., 2, ...),  -- owner1
(6, '台南古蹟民宿', ..., 3, ...),  -- owner2
(7, '墾丁海景度假村', ..., 2, ...),-- owner1
(8, '宜蘭溫泉飯店', ..., 3, ...);  -- owner2
```

**現在外鍵引用正確**:
- `owner_id = 2` → 對應 owner1 ✓
- `owner_id = 3` → 對應 owner2 ✓

---

### 3. room_types 表

**修改**:
```sql
INSERT INTO room_types (id, name, ..., accommodation_id) VALUES
(1, '標準房', ..., 1),   -- 台北商旅
(2, '豪華房', ..., 1),   -- 台北商旅
(3, '海景房', ..., 2),   -- 高雄港景飯店
-- ... 省略
(15, '溫泉套房', ..., 8); -- 宜蘭溫泉飯店
```

---

## 🎯 ID 分配策略

### 完整的 ID 對應表

| 表 | ID 範圍 | 數量 | 說明 |
|------|---------|------|------|
| users | 1-6 | 6 個 | 1=admin, 2-3=owner, 4-6=user |
| accommodations | 1-8 | 8 個 | 8 個住宿 |
| room_types | 1-15 | 15 個 | 每個住宿 1-2 個房型 |
| bookings | 自動 | 22 個 | 保留自動遞增 |
| reviews | 自動 | 18 個 | 保留自動遞增 |

### 為什麼 bookings 和 reviews 不指定 ID？

**原因**:
- 這些表沒有被其他表引用為外鍵
- 不需要固定 ID
- 保持靈活性

---

## 🧪 驗證測試

### 編譯測試 ✅

```
[INFO] Building booking 0.0.1-SNAPSHOT
[INFO] Compiling 46 source files
[INFO] BUILD SUCCESS
[INFO] Total time:  1.915 s
```

### 啟動測試（待執行）

**預期結果**:
- ✅ data.sql 成功執行
- ✅ 6 個用戶插入成功
- ✅ 8 個住宿插入成功（外鍵正確）
- ✅ 15 個房型插入成功
- ✅ 評論插入成功

---

## ⚠️ 注意事項

### 1. 清空資料庫

**重要**：修改後必須先清空資料庫！

```sql
-- 方法 1: 刪除並重建資料庫（推薦）
DROP DATABASE booking_db;
CREATE DATABASE booking_db;

-- 方法 2: 僅清空表
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE reviews;
TRUNCATE TABLE bookings;
TRUNCATE TABLE room_types;
TRUNCATE TABLE accommodations;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;
```

### 2. 自動遞增重置

刪除資料庫並重建會自動重置所有自動遞增計數器。

### 3. 生產環境考量

**不建議在生產環境使用固定 ID**:
- 可能與現有資料衝突
- 使用 Flyway 進行資料遷移
- 或使用 UUID 作為主鍵

---

## 📊 修改總結

### 修改的檔案

| 檔案 | 變更 | 說明 |
|------|------|------|
| data.sql | 3 處修改 | users, accommodations, room_types 明確指定 ID |

### 修改的 SQL 語句

1. **users**: 新增 `id` 欄位到 INSERT 語句
2. **accommodations**: 新增 `id` 欄位到 INSERT 語句
3. **room_types**: 新增 `id` 欄位到 INSERT 語句

### 未修改

- **bookings**: 保持自動遞增
- **reviews**: 保持自動遞增

---

## 🚀 後續步驟

### 立即執行

1. **清空資料庫**
   ```sql
   DROP DATABASE booking_db;
   CREATE DATABASE booking_db;
   ```

2. **重啟應用程式**
   - data.sql 會自動執行
   - 所有資料正確插入

3. **驗證資料**
   ```sql
   -- 檢查用戶
   SELECT id, username, role FROM users ORDER BY id;
   
   -- 檢查住宿
   SELECT id, name, owner_id FROM accommodations ORDER BY id;
   
   -- 檢查房型
   SELECT id, name, accommodation_id FROM room_types ORDER BY id;
   
   -- 檢查評論
   SELECT COUNT(*) FROM reviews;
   ```

### 預期結果

**用戶** (6 個):
```
1 | admin  | ROLE_ADMIN
2 | owner1 | ROLE_OWNER
3 | owner2 | ROLE_OWNER
4 | user1  | ROLE_USER
5 | user2  | ROLE_USER
6 | user3  | ROLE_USER
```

**住宿** (8 個):
```
1 | 台北商旅       | 2
2 | 高雄港景飯店   | 2
3 | 台中精品旅館   | 3
4 | 花蓮民宿       | 3
5 | 台北經濟旅館   | 2
6 | 台南古蹟民宿   | 3
7 | 墾丁海景度假村 | 2
8 | 宜蘭溫泉飯店   | 3
```

**評論**: 18 則

---

## 💡 學習要點

### 外鍵約束的重要性

1. **資料完整性**: 確保引用的資料存在
2. **插入順序**: 必須先插入父表，再插入子表
3. **ID 對應**: 外鍵值必須精確對應主鍵值

### 自動遞增的陷阱

1. **不可預測**: 刪除資料後計數器不重置
2. **測試困難**: 每次測試 ID 可能不同
3. **解決方案**: 明確指定 ID（測試環境）

### 最佳實踐

1. **測試環境**: 使用固定 ID，方便測試
2. **生產環境**: 使用自動遞增或 UUID
3. **資料遷移**: 使用 Flyway/Liquibase

---

## ✨ 總結

### 問題

- ❌ data.sql 插入失敗
- ❌ 外鍵約束錯誤
- ❌ `owner_id = 2` 不存在

### 原因

- ❌ 用戶 ID 自動生成，不可預測
- ❌ 住宿引用錯誤的 owner_id

### 修復

- ✅ 明確指定 users 的 ID (1-6)
- ✅ 明確指定 accommodations 的 ID (1-8)
- ✅ 明確指定 room_types 的 ID (1-15)
- ✅ 確保外鍵引用正確

### 結果

- ✅ 編譯成功
- ⏳ 待清空資料庫並重啟驗證

---

**修復日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 已修復並編譯成功


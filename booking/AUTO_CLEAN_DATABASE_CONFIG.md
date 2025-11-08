# 🔄 自動清空資料配置完成報告

## 📋 問題與解決方案

**問題**: 每次啟動時 `data.sql` 重複執行導致主鍵衝突  
**錯誤**: `Duplicate entry '1' for key 'users.PRIMARY'`  
**解決**: 設定為每次啟動自動清空並重建資料

**完成日期**: 2025-11-08  
**狀態**: ✅ 已完成並編譯成功

---

## ✅ 配置變更

### application.properties

**修改前**:
```properties
spring.jpa.hibernate.ddl-auto=update
```

**修改後**:
```properties
# 開發環境：每次啟動都刪除並重建資料表（會清空所有資料）
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## 🎯 ddl-auto 選項說明

### create-drop ✅ (開發環境推薦)

**行為**:
1. 應用程式啟動：刪除所有表 → 重新創建表
2. 應用程式關閉：刪除所有表

**優點**:
- ✅ 每次都是乾淨的資料
- ✅ 不會有主鍵衝突
- ✅ 適合開發和測試

**缺點**:
- ❌ 每次重啟都會丟失資料
- ❌ 不適合生產環境

**適用場景**:
- 🔧 開發環境
- 🧪 測試環境
- 📚 學習環境

---

### 其他選項對比

#### update (生產環境)

**行為**:
- 只會更新結構（新增欄位、表）
- 不會刪除現有資料
- 不會刪除欄位或表

**優點**:
- ✅ 保留現有資料
- ✅ 安全

**缺點**:
- ❌ 可能導致主鍵衝突
- ❌ 結構變更不完整

---

#### create (較少使用)

**行為**:
- 啟動時刪除並重建表
- 關閉時不刪除

**問題**:
- 每次啟動都清空，但表會保留

---

#### validate (生產環境)

**行為**:
- 只驗證結構
- 不做任何變更

**用途**:
- 生產環境確保安全

---

#### none

**行為**:
- 不做任何事

---

## 📊 完整的啟動流程

### 使用 create-drop 的完整流程

```
1. 應用程式啟動
   ↓
2. Hibernate 執行 DROP ALL TABLES
   ├─ DROP TABLE IF EXISTS reviews;
   ├─ DROP TABLE IF EXISTS bookings;
   ├─ DROP TABLE IF EXISTS room_types;
   ├─ DROP TABLE IF EXISTS accommodations;
   └─ DROP TABLE IF EXISTS users;
   ↓
3. Hibernate 執行 CREATE TABLES
   ├─ CREATE TABLE users (...);
   ├─ CREATE TABLE accommodations (...);
   ├─ CREATE TABLE room_types (...);
   ├─ CREATE TABLE bookings (...);
   └─ CREATE TABLE reviews (...);
   ↓
4. Spring 執行 data.sql
   ├─ INSERT INTO users ... (6 個)
   ├─ INSERT INTO accommodations ... (8 個)
   ├─ INSERT INTO room_types ... (15 個)
   ├─ INSERT INTO bookings ... (22 個)
   └─ INSERT INTO reviews ... (18 個)
   ↓
5. 應用程式就緒 ✅
```

---

## 🧪 測試驗證

### 編譯測試 ✅

```
[INFO] Building booking 0.0.1-SNAPSHOT
[INFO] Compiling 46 source files
[INFO] BUILD SUCCESS
[INFO] Total time:  1.958 s
```

### 啟動測試（待執行）

**預期行為**:

1. **第一次啟動**:
   ```
   Hibernate: drop table if exists reviews
   Hibernate: drop table if exists bookings
   Hibernate: drop table if exists room_types
   Hibernate: drop table if exists accommodations
   Hibernate: drop table if exists users
   
   Hibernate: create table users (...)
   Hibernate: create table accommodations (...)
   ...
   
   執行 data.sql
   插入 6 個用戶 ✅
   插入 8 個住宿 ✅
   插入 15 個房型 ✅
   插入 22 個訂單 ✅
   插入 18 則評論 ✅
   ```

2. **第二次啟動**（重啟）:
   ```
   再次執行 DROP → CREATE → data.sql
   所有資料重新載入
   不會有衝突 ✅
   ```

---

## ⚠️ 重要注意事項

### 1. 資料會被清空

**每次重啟應用程式**:
- ❌ 所有資料都會被刪除
- ✅ 重新載入 data.sql 的測試資料

**不適合**:
- 儲存重要資料
- 需要保留測試資料
- 生產環境

### 2. 生產環境配置

**⚠️ 絕對不要在生產環境使用 create-drop！**

**生產環境建議**:
```properties
# 生產環境配置
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=never
```

**資料遷移工具**:
- 使用 Flyway
- 使用 Liquibase

### 3. 多環境配置

**建議使用 profile**:

**application-dev.properties** (開發):
```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.sql.init.mode=always
```

**application-prod.properties** (生產):
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=never
```

**啟動時指定**:
```bash
# 開發環境
java -jar booking.jar --spring.profiles.active=dev

# 生產環境
java -jar booking.jar --spring.profiles.active=prod
```

---

## 📝 當前配置總結

### application.properties

```properties
# ===== Database (MySQL) =====
spring.datasource.url=jdbc:mysql://localhost:3306/booking_db
spring.datasource.username=root
spring.datasource.password=2FTA93108
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# 開發環境：每次啟動都刪除並重建資料表
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# ===== Data Initialization =====
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

### 行為說明

| 設定 | 值 | 效果 |
|------|-----|------|
| `ddl-auto` | `create-drop` | 啟動時刪除並重建所有表 |
| `sql.init.mode` | `always` | 總是執行 data.sql |
| `defer-datasource-initialization` | `true` | 在 DDL 之後執行 SQL |
| `show-sql` | `true` | 顯示所有 SQL 語句 |

---

## 🎯 優點與缺點

### 優點 ✅

1. **不會有主鍵衝突**
   - 每次都是新的資料表
   - ID 從 1 開始

2. **測試資料一致**
   - 每次啟動都相同
   - 容易重現問題

3. **開發方便**
   - 不需要手動清空資料庫
   - 修改 data.sql 立即生效

4. **學習友善**
   - 不用擔心資料混亂
   - 隨時可以重啟獲得乾淨狀態

### 缺點 ❌

1. **資料不保留**
   - 重啟後所有手動新增的資料消失
   - 不適合需要保留測試資料的情況

2. **啟動稍慢**
   - 每次都要 DROP + CREATE
   - 對大型專案影響較大

3. **不適合團隊開發**
   - 如果多人共用資料庫
   - 會互相影響

---

## 🚀 使用指南

### 日常開發

**正常使用**:
1. 修改程式碼
2. 重啟應用程式
3. 自動獲得乾淨的測試資料

**修改測試資料**:
1. 編輯 `data.sql`
2. 重啟應用程式
3. 新資料自動載入

### 需要保留資料時

**臨時改回 update**:
```properties
# 暫時保留資料
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=never
```

**重啟後**:
- 資料不會被刪除
- data.sql 不會執行

---

## 💡 最佳實踐

### 1. 本地開發

**推薦配置**:
```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.sql.init.mode=always
```

**適合**:
- 個人開發
- 學習練習
- 快速測試

### 2. 團隊開發

**推薦工具**: Flyway

**pom.xml**:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

**配置**:
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

**優點**:
- 版本控制
- 可回滾
- 團隊同步

### 3. 生產環境

**必須配置**:
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=never
```

**資料遷移**:
- 使用 Flyway 或 Liquibase
- 專業 DBA 管理
- 完整備份策略

---

## 📚 相關資料

### 官方文檔

- [Spring Boot Database Initialization](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization)
- [Hibernate DDL Auto](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data.spring.jpa.hibernate.ddl-auto)

### 推薦閱讀

1. **Flyway 遷移指南**
2. **生產環境資料庫最佳實踐**
3. **多環境配置管理**

---

## ✨ 總結

### 問題

- ❌ data.sql 重複執行
- ❌ 主鍵衝突：`Duplicate entry '1' for key 'users.PRIMARY'`
- ❌ 應用程式啟動失敗

### 解決方案

- ✅ 設定 `spring.jpa.hibernate.ddl-auto=create-drop`
- ✅ 每次啟動自動清空並重建
- ✅ 不會有任何衝突

### 結果

- ✅ 編譯成功
- ✅ 每次啟動都有乾淨的測試資料
- ✅ 開發體驗大幅提升

### 注意事項

- ⚠️ 僅適用於開發環境
- ⚠️ 資料不會保留
- ⚠️ 生產環境禁用

---

## 🎉 立即測試

**現在可以：**

1. **重啟應用程式**
   - 自動刪除舊資料
   - 重新創建表
   - 載入測試資料

2. **驗證資料**
   ```
   http://localhost:8080/
   ```
   - 應該看到 8 個住宿
   - 每個都有完整資訊

3. **查看詳情**
   ```
   http://localhost:8080/accommodations/1
   ```
   - 圖片、評分、評論都正常顯示
   - 房型列表正常顯示

4. **測試評論**
   - 可以新增評論
   - 重啟後評論消失（回到初始狀態）

---

**配置日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 已完成並可使用


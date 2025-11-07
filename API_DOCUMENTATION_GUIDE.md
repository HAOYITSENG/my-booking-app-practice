# API 文件使用指南

## 📚 文件訪問

### Swagger UI
- **開發環境**: http://localhost:8080/swagger-ui.html
- **互動式 API 文件，支援線上測試**

### OpenAPI JSON
- **開發環境**: http://localhost:8080/v3/api-docs
- **標準 OpenAPI 3.0 格式，可匯入 Postman**

---

## 🚀 快速開始

### 1. 在 Swagger UI 中測試 API

#### 步驟一：先登入系統
由於使用 Session-based 認證，需先在同個瀏覽器登入：

1. 開啟新分頁訪問：http://localhost:8080/login
2. 使用測試帳號登入（例如：`admin` / `admin123`）
3. 回到 Swagger UI 分頁

#### 步驟二：測試公開 API
不需登入即可測試：

1. 展開 `Accommodations` 分組
2. 點擊 `GET /api/accommodations`
3. 點擊「Try it out」
4. 點擊「Execute」
5. 查看回應結果

#### 步驟三：測試需要認證的 API
需要先登入：

1. 展開 `Bookings` 分組
2. 點擊 `GET /api/bookings`
3. 點擊「Try it out」
4. 點擊「Execute」
5. 如果已登入，會看到訂單列表
6. 如果未登入，會收到 401 錯誤

---

## 🔐 認證與授權

### 認證方式
- **類型**: Session-based (Cookie)
- **Cookie 名稱**: JSESSIONID
- **有效期**: Session（關閉瀏覽器失效）

### 角色與權限

| 角色 | 權限範圍 |
|------|----------|
| **ROLE_ADMIN** | 所有 API，包含 `/api/admin/**` |
| **ROLE_OWNER** | 自己的住宿與訂單，包含 `/api/owner/**` |
| **ROLE_USER** | 查詢住宿、建立訂單、管理自己的訂單 |
| **匿名** | 只能查詢住宿與房型 |

---

## 📋 API 分組說明

### 1. Authentication (認證)
- `POST /api/auth/register` - 註冊新帳號
- `GET /api/auth/me` - 取得目前登入者資訊

### 2. Accommodations (住宿)
所有 API 皆為公開（不需登入）

- `GET /api/accommodations` - 取得所有住宿
- `GET /api/accommodations/{id}` - 取得單一住宿
- `GET /api/accommodations/search` - 搜尋住宿
- `GET /api/accommodations/available` - 查詢可用住宿

### 3. Room Types (房型)
所有 API 皆為公開（不需登入）

- `GET /api/room-types` - 取得所有房型
- `GET /api/room-types/by-accommodation/{accId}` - 查詢住宿的房型

### 4. Bookings (訂單)
需要登入

- `POST /api/bookings/by-room-type` - 建立訂單
- `GET /api/bookings` - 取得訂單列表（根據角色）
- `DELETE /api/bookings/{id}` - 取消訂單（一般用戶）
- `DELETE /api/bookings/admin/{id}` - 取消訂單（管理員）
- `GET /api/bookings/admin/all` - 查看所有訂單（管理員）

### 5. Statistics (統計)
需要登入

- `GET /api/statistics/order-status` - 訂單狀態分布
- `GET /api/statistics/orders-trend` - 訂單趨勢
- `GET /api/statistics/top-accommodations` - 熱門住宿（管理員）
- `GET /api/statistics/monthly-revenue` - 月度營收
- `GET /api/statistics/admin/dashboard` - 管理員儀表板（管理員）
- `GET /api/statistics/owner/dashboard` - 房東儀表板（房東）

### 6. Export (匯出)
需要登入

- `GET /api/export/bookings` - 匯出訂單
- `GET /api/export/admin/bookings` - 匯出所有訂單（管理員）
- `GET /api/export/owner/revenue` - 匯出營收報表（房東）

---

## 💡 使用技巧

### 在 Postman 中使用

1. **匯入 OpenAPI 規格**
   - 訪問：http://localhost:8080/v3/api-docs
   - 複製 JSON 內容
   - Postman → Import → Raw text → 貼上 JSON

2. **設定 Cookie 認證**
   - 先使用瀏覽器登入系統
   - 開啟瀏覽器開發者工具（F12）
   - Application → Cookies → 複製 JSESSIONID 的值
   - Postman → Headers → 新增 `Cookie: JSESSIONID=你的值`

### 常見查詢參數

#### 日期格式
- **格式**: ISO 8601 (YYYY-MM-DD)
- **範例**: `2025-01-15`

#### 訂單狀態
- `PENDING` - 待確認
- `CONFIRMED` - 已確認
- `CANCELLED` - 已取消

---

## 🧪 測試案例

### 案例 1：查詢並預訂住宿

```bash
# 1. 查詢所有住宿
GET /api/accommodations

# 2. 查詢特定住宿的房型
GET /api/room-types/by-accommodation/1

# 3. 建立訂單（需先登入）
POST /api/bookings/by-room-type
  ?roomTypeId=1
  &checkIn=2025-01-15
  &checkOut=2025-01-18
  &quantity=2

# 4. 查看訂單
GET /api/bookings
```

### 案例 2：管理員查看統計

```bash
# 1. 以管理員登入

# 2. 取得所有統計資料
GET /api/statistics/admin/dashboard

# 3. 查看熱門住宿
GET /api/statistics/top-accommodations?limit=10

# 4. 匯出訂單報表
GET /api/export/admin/bookings
```

### 案例 3：房東管理訂單

```bash
# 1. 以房東登入

# 2. 查看自己的訂單
GET /api/bookings

# 3. 確認訂單
POST /api/owner/bookings/{id}/confirm

# 4. 匯出營收報表
GET /api/export/owner/revenue
```

---

## ⚠️ 常見問題

### Q1: 為什麼 API 回傳 401 Unauthorized？
**A**: 需要先登入。請在同個瀏覽器開啟 http://localhost:8080/login 登入後再測試。

### Q2: 為什麼 API 回傳 403 Forbidden？
**A**: 權限不足。檢查：
- 是否使用正確角色的帳號
- API 是否需要特定權限（如 ROLE_ADMIN）

### Q3: 如何在 Swagger UI 中保持登入狀態？
**A**: 
1. 在同個瀏覽器先登入系統
2. 不要登出
3. Swagger UI 會自動使用瀏覽器的 Session Cookie

### Q4: 訂單建立失敗，顯示「庫存不足」？
**A**: 
- 檢查房型的 `totalRooms` 是否足夠
- 檢查該日期區間是否已有其他訂單
- 嘗試減少預訂數量

### Q5: 日期參數格式錯誤？
**A**: 確保使用 ISO 8601 格式：`YYYY-MM-DD`，例如 `2025-01-15`

---

## 📝 API 設計原則

本系統 API 遵循以下設計原則：

1. **RESTful 風格**
   - 使用標準 HTTP 方法（GET, POST, PUT, DELETE）
   - 資源導向的 URL 設計

2. **一致的回應格式**
   - 成功：回傳 200 + 資料
   - 錯誤：回傳對應狀態碼 + 錯誤訊息

3. **明確的錯誤訊息**
   - 提供中文錯誤說明
   - 包含錯誤原因與建議

4. **權限分離**
   - 不同角色看到不同資料
   - API 層級權限控管

5. **效能優化**
   - Dashboard API 一次回傳所有資料
   - 使用分頁（未來擴充）

---

## 🔗 相關資源

- [OpenAPI 規格](https://spec.openapis.org/oas/v3.1.0)
- [SpringDoc 官方文件](https://springdoc.org/)
- [Swagger UI 教學](https://swagger.io/docs/open-source-tools/swagger-ui/usage/installation/)

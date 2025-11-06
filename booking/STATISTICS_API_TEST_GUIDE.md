# 📊 Statistics API 測試指南

## 🚀 啟動應用程式

請在命令提示字元中執行以下命令：

```bash
cd C:\my-booking-app-practice\booking
mvn spring-boot:run
```

或者如果使用 IDE（IntelliJ IDEA），可以直接執行 `BookingApplication.java` 的 main 方法。

## 🧪 API 測試步驟

### 步驟 1：登入系統
訪問：http://localhost:8080/login

**管理員帳號**：
- 用戶名：`admin`
- 密碼：`password123`

**房東帳號**：
- 用戶名：`owner1`
- 密碼：`password123`

### 步驟 2：測試管理員 API

使用瀏覽器或 Postman 測試以下端點：

#### 2.1 訂單狀態分布
```
GET http://localhost:8080/api/statistics/order-status
```

預期回應：
```json
{
  "PENDING": 2,
  "CONFIRMED": 14,
  "CANCELLED": 2
}
```

#### 2.2 訂單趨勢（近30天）
```
GET http://localhost:8080/api/statistics/orders-trend
```

預期回應：
```json
[
  {
    "date": "10-08",
    "new": 0,
    "confirmed": 0,
    "cancelled": 0
  },
  {
    "date": "10-09", 
    "new": 0,
    "confirmed": 0,
    "cancelled": 0
  },
  ...
  {
    "date": "11-01",
    "new": 2,
    "confirmed": 2,
    "cancelled": 0
  }
]
```

#### 2.3 熱門住宿排行
```
GET http://localhost:8080/api/statistics/top-accommodations
```

預期回應：
```json
[
  {
    "name": "台北商旅",
    "count": 6
  },
  {
    "name": "高雄港景飯店", 
    "count": 4
  },
  {
    "name": "台中精品旅館",
    "count": 4
  },
  {
    "name": "花蓮民宿",
    "count": 4
  }
]
```

#### 2.4 月度營收
```
GET http://localhost:8080/api/statistics/monthly-revenue
```

預期回應：
```json
[
  {
    "month": "2025-08",
    "revenue": 8900.0
  },
  {
    "month": "2025-09", 
    "revenue": 20200.0
  },
  {
    "month": "2025-10",
    "revenue": 31400.0
  },
  {
    "month": "2025-11",
    "revenue": 25800.0
  }
]
```

#### 2.5 管理員儀表板（一次取得所有資料）
```
GET http://localhost:8080/api/statistics/admin/dashboard
```

預期回應：包含上述所有統計資料的組合 JSON

### 步驟 3：測試房東 API

登出管理員，登入房東帳號 `owner1`，然後測試：

#### 3.1 房東訂單狀態
```
GET http://localhost:8080/api/statistics/order-status
```

#### 3.2 房東住宿營收佔比
```
GET http://localhost:8080/api/statistics/accommodation-revenue
```

#### 3.3 房東房型銷售排行
```
GET http://localhost:8080/api/statistics/room-type-sales
```

#### 3.4 房東入住率趨勢
```
GET http://localhost:8080/api/statistics/occupancy-rate
```

#### 3.5 房東儀表板
```
GET http://localhost:8080/api/statistics/owner/dashboard
```

## 🐛 常見問題排除

### 問題 1：403 Forbidden
- 確認已正確登入
- 確認使用正確的用戶角色（管理員 vs 房東）
- 重新啟動應用程式

### 問題 2：500 Internal Server Error
- 檢查控制台錯誤訊息
- 確認資料庫中有測試資料
- 檢查 StatisticsService 的計算邏輯

### 問題 3：空資料 []
- 確認 data.sql 中有測試資料
- 檢查 created_at 欄位是否正確設定
- 重新啟動應用程式重新載入資料

## ✅ 成功標準

所有 API 應該：
- 回應 HTTP 200 狀態碼
- 回傳正確格式的 JSON 資料
- 資料數值合理（不是全部為 0）
- 日期格式正確（MM-dd 或 yyyy-MM）

完成測試後請回報結果給開發者！

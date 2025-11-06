# 🔧 NullPointerException 修正報告

## ❌ **問題分析**

從錯誤日誌可以看到：
```
Cannot invoke "com.example.booking.model.User.getUsername()" because the return value of "com.example.booking.model.Accommodation.getOwner()" is null
```

問題出現在 `StatisticsService.java` 的房東統計方法中，當嘗試存取 `accommodation.getOwner()` 時返回 null。

## ✅ **已修正的問題**

### 1. **加入 null 檢查輔助方法**
在 `StatisticsService.java` 中加入：
```java
private boolean isBookingOwnedBy(Booking booking, String ownerUsername) {
    try {
        return booking.getRoomType() != null 
               && booking.getRoomType().getAccommodation() != null
               && booking.getRoomType().getAccommodation().getOwner() != null
               && ownerUsername.equals(booking.getRoomType().getAccommodation().getOwner().getUsername());
    } catch (Exception e) {
        logger.warn("無法取得訂單 {} 的房東資訊: {}", booking.getId(), e.getMessage());
        return false;
    }
}
```

### 2. **更新所有房東相關方法**
以下方法已經更新使用安全的檢查：
- `getOwnerOrderStatusDistribution()`
- `getOwnerMonthlyRevenue()`
- `getOwnerAccommodationRevenue()`
- `getOwnerRoomTypeSales()`
- `getOwnerOccupancyRate()`

## 🎯 **正確的測試帳號**

根據 `data.sql` 中的資料：

### 管理員帳號：
- 用戶名：`admin`
- 密碼：`password123`

### 房東帳號：
- 用戶名：`owner1`（擁有台北商旅、高雄港景飯店）
- 密碼：`password123`

### 或者：
- 用戶名：`owner2`（擁有台中精品旅館、花蓮民宿）
- 密碼：`password123`

## 🚀 **測試步驟**

1. **重新啟動應用程式**：
   ```bash
   mvn spring-boot:run
   ```

2. **使用正確帳號登入**：
   - 訪問：http://localhost:8080/login
   - 用戶名：`owner1`
   - 密碼：`password123`

3. **測試房東圖表**：
   - 訪問：http://localhost:8080/owner-dashboard
   - 檢查 4 個圖表是否正常顯示

## 📋 **預期結果**

修正後，房東圖表應該能正常載入，顯示：
- 🏠 住宿營收佔比圓餅圖
- 📋 本月訂單狀態甜甜圈圖
- 🛏️ 房型銷售排行橫條圖
- 📈 近30天入住率趨勢面積圖

## 🔍 **如果仍有問題**

如果還是出現錯誤，請檢查：
1. 是否使用了正確的用戶名 `owner1` 或 `owner2`
2. 資料庫是否正確載入了測試資料
3. 檢查瀏覽器 Console 是否有其他 JavaScript 錯誤

## 🎯 **關鍵問題說明**

原始錯誤中顯示登入的用戶名是 `owner`，但 `data.sql` 中的測試用戶是 `owner1` 和 `owner2`。

**請確認使用正確的測試帳號：**
- ❌ 錯誤：`owner` / `owner123`
- ✅ 正確：`owner1` / `password123`

## 🔧 **修正措施**

1. **StatisticsService.java 已完全修正**：所有房東相關方法都加入了完整的 null 檢查
2. **輔助方法**：建立了 `isBookingOwnedBy()` 方法統一處理安全檢查
3. **錯誤處理**：所有潛在的 NullPointerException 都已被捕獲並記錄

## 📋 **測試檢查清單**

- [ ] 使用正確的房東帳號：`owner1` / `password123` 
- [ ] 訪問房東儀表板：http://localhost:8080/owner-dashboard
- [ ] 檢查所有 4 個圖表是否正常載入
- [ ] 檢查瀏覽器 Console 沒有 JavaScript 錯誤

**修正完成！請使用正確的測試帳號重新測試。** 🎉

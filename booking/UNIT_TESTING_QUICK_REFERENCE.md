# 單元測試快速參考指南

## 📦 測試文件總覽

| 測試類 | 檔案路徑 | 測試數量 | 狀態 |
|--------|---------|----------|------|
| BookingServiceTest | `src/test/java/.../BookingServiceTest.java` | 24 | ✅ 已完成 |
| StatisticsServiceTest | `src/test/java/.../StatisticsServiceTest.java` | 18 | ✅ 已完成 |
| ExportServiceTest | `src/test/java/.../ExportServiceTest.java` | 19 | ✅ 已完成 |
| RoomTypeManagementTest | `src/test/java/.../RoomTypeManagementTest.java` | 20 | ✅ 已完成 |
| **總計** | - | **81** | **✅ 100% 通過** |

---

## ⚡ 快速測試命令

### Windows (cmd.exe)

```cmd
# 執行所有測試
cd C:\my-booking-app-practice\booking
mvn test

# 執行特定測試類
mvn test -Dtest=BookingServiceTest
mvn test -Dtest=StatisticsServiceTest
mvn test -Dtest=ExportServiceTest
mvn test -Dtest=RoomTypeManagementTest

# 執行特定測試方法
mvn test -Dtest=BookingServiceTest#testBookByRoomType_Success

# 跳過測試（僅編譯）
mvn compile -DskipTests

# 清理並測試
mvn clean test

# 生成測試報告
mvn surefire-report:report
```

### 查看測試結果

```cmd
# 查看測試報告文字檔
type target\surefire-reports\TEST-com.example.booking.service.BookingServiceTest.txt

# 打開 HTML 報告（需先執行 mvn surefire-report:report）
start target\site\surefire-report.html
```

---

## 🎯 測試涵蓋功能速查表

### 1️⃣ BookingServiceTest (24 tests)

#### 訂單建立 ✅
- ✓ 正常建立訂單
- ✓ 價格計算正確性
- ✓ 庫存檢查（不足、剛好、已滿）
- ✓ 參數驗證（日期、數量）
- ✓ 實體存在性檢查

#### 訂單取消 ✅
- ✓ 用戶取消自己的訂單
- ✓ 權限驗證（無法取消他人訂單）
- ✓ 狀態檢查（已取消、已開始）
- ✓ 管理員取消任意訂單

#### 價格計算 ✅
- ✓ 單間多晚
- ✓ 多間多晚
- ✓ 單晚單間

---

### 2️⃣ StatisticsServiceTest (18 tests)

#### 訂單統計 ✅
- ✓ 狀態分布（PENDING/CONFIRMED/CANCELLED）
- ✓ 房東訂單統計
- ✓ 空資料處理

#### 趨勢分析 ✅
- ✓ N 天訂單趨勢
- ✓ 單日趨勢
- ✓ 空趨勢處理

#### 排行榜 ✅
- ✓ 熱門住宿 Top N
- ✓ 房型銷售排行
- ✓ 按數量排序

#### 營收統計 ✅
- ✓ 月度營收
- ✓ 房東營收
- ✓ 住宿營收分布
- ✓ 只計算 CONFIRMED 訂單

#### 快取機制 ✅
- ✓ 第二次調用使用快取

---

### 3️⃣ ExportServiceTest (19 tests)

#### 管理員匯出 ✅
- ✓ 匯出所有訂單
- ✓ 狀態篩選
- ✓ 日期範圍篩選
- ✓ 多重條件篩選
- ✓ 空結果處理

#### 用戶匯出 ✅
- ✓ 只匯出個人訂單
- ✓ 日期篩選
- ✓ 空訂單處理

#### 房東匯出 ✅
- ✓ 只匯出房東訂單
- ✓ 日期篩選
- ✓ 空訂單處理

#### Excel 驗證 ✅
- ✓ 檔案結構正確
- ✓ 檔案大小合理

#### 篩選邏輯 ✅
- ✓ 日期包含性（inclusive）
- ✓ 狀態篩選
- ✓ 無效範圍處理

---

### 4️⃣ RoomTypeManagementTest (20 tests)

#### 房型管理 ✅
- ✓ 新增房型
- ✓ 查詢房型列表
- ✓ 權限驗證
- ✓ 實體存在性

#### 住宿管理 ✅
- ✓ 新增住宿
- ✓ 更新住宿
- ✓ 刪除住宿
- ✓ 所有權驗證

#### 查詢功能 ✅
- ✓ 房東住宿列表
- ✓ 所有住宿列表
- ✓ 地點搜尋
- ✓ 空結果處理

---

## 🔥 常見測試場景

### 場景 1: 庫存不足測試

```java
// 已訂 3 間，總共 5 間，要訂 3 間 → 超過限制
@Test
void testBookByRoomType_InsufficientInventory_ThrowsException() {
    when(bookingRepo.sumBookedQuantityBetween(...)).thenReturn(3L);
    
    assertThatThrownBy(() -> 
        service.bookByRoomType(1L, checkIn, checkOut, 3))
        .hasMessageContaining("庫存不足");
}
```

### 場景 2: 權限檢查測試

```java
// 用戶 A 無法取消用戶 B 的訂單
@Test
void testCancelBooking_ByOtherUser_ThrowsException() {
    assertThatThrownBy(() -> 
        service.cancelBooking(1L, "otheruser"))
        .hasMessageContaining("沒有權限取消此訂單");
}
```

### 場景 3: 價格計算測試

```java
// 3 晚 × 2000/晚 × 2 間 = 12000
@Test
void testBookByRoomType_PriceCalculation() {
    Booking result = service.bookByRoomType(1L, checkIn, checkOut, 2);
    
    assertThat(result.getTotalPrice())
        .isEqualByComparingTo(BigDecimal.valueOf(12000));
}
```

---

## 📊 測試結果範例

### 成功執行結果

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.example.booking.service.BookingServiceTest
[INFO] Tests run: 24, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.654 s
[INFO] 
[INFO] Running com.example.booking.service.StatisticsServiceTest
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.421 s
[INFO] 
[INFO] Running com.example.booking.service.ExportServiceTest
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.523 s
[INFO] 
[INFO] Running com.example.booking.service.RoomTypeManagementTest
[INFO] Tests run: 20, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.389 s
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 81, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 失敗範例（用於除錯）

```
[ERROR] testBookByRoomType_Success  Time elapsed: 0.045 s  <<< FAILURE!
java.lang.AssertionError: 
Expecting:
  <6000>
to be equal to:
  <12000>
```

---

## 🛠️ 測試開發指南

### 新增測試步驟

1. **確定測試類別**
   ```java
   @ExtendWith(MockitoExtension.class)
   @DisplayName("服務名稱單元測試")
   class ServiceTest { }
   ```

2. **設置 Mock 物件**
   ```java
   @Mock
   private Repository repository;
   
   @InjectMocks
   private Service service;
   ```

3. **編寫測試方法**
   ```java
   @Test
   @DisplayName("測試目的描述")
   void testMethodName_Scenario_ExpectedResult() {
       // Given - 準備
       // When - 執行
       // Then - 驗證
   }
   ```

### 測試命名規範

```
test{方法名}_{測試場景}_{預期結果}
```

範例：
- `testBookByRoomType_Success`
- `testBookByRoomType_InsufficientInventory_ThrowsException`
- `testCancelBooking_ByOwner_Success`

---

## 🎓 Assert 常用方法

### AssertJ 斷言

```java
// 基本斷言
assertThat(result).isNotNull();
assertThat(result).isEqualTo(expected);
assertThat(list).hasSize(5);
assertThat(list).isEmpty();
assertThat(list).isNotEmpty();

// 數字斷言
assertThat(number).isGreaterThan(10);
assertThat(number).isLessThanOrEqualTo(100);
assertThat(price).isEqualByComparingTo(BigDecimal.valueOf(2000));

// 字串斷言
assertThat(message).contains("錯誤");
assertThat(message).startsWith("ERROR:");
assertThat(message).matches("\\d{4}-\\d{2}");

// 異常斷言
assertThatThrownBy(() -> service.method())
    .isInstanceOf(RuntimeException.class)
    .hasMessageContaining("預期錯誤");

// 無異常斷言
assertThatNoException().isThrownBy(() -> service.method());
```

### Mockito 驗證

```java
// 驗證調用次數
verify(repository, times(1)).save(any());
verify(repository, never()).delete(any());

// 驗證調用順序
InOrder inOrder = inOrder(repo1, repo2);
inOrder.verify(repo1).save(any());
inOrder.verify(repo2).update(any());

// 參數捕獲
ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
verify(repository).save(captor.capture());
assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
```

---

## 🐛 常見問題排查

### 問題 1: NullPointerException

**原因**: Mock 物件未正確設置返回值

**解決**:
```java
// 錯誤
when(repo.findById(1L)).thenReturn(entity); // entity 可能是 null

// 正確
when(repo.findById(1L)).thenReturn(Optional.of(entity));
```

### 問題 2: 測試通過但覆蓋率低

**原因**: 只測試了成功路徑，忽略異常情況

**解決**: 補充異常測試
```java
@Test
void testMethod_WhenException_ThrowsException() {
    when(repo.findById(99L)).thenReturn(Optional.empty());
    
    assertThatThrownBy(() -> service.method(99L))
        .isInstanceOf(RuntimeException.class);
}
```

### 問題 3: 測試偶爾失敗

**原因**: 測試依賴外部狀態或時間

**解決**: 使用 Mock 時間或固定值
```java
// 不好的做法
LocalDate today = LocalDate.now(); // 每天都不同

// 好的做法
LocalDate fixedDate = LocalDate.of(2025, 1, 8);
```

---

## 📚 延伸閱讀

- [JUnit 5 官方文件](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 官方文件](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ 官方文件](https://assertj.github.io/doc/)
- [單元測試完整說明](./UNIT_TESTING_COMPLETE_GUIDE.md)

---

## ✅ 檢查清單

開發新功能時的測試清單：

- [ ] 成功路徑測試
- [ ] 失敗路徑測試（異常情況）
- [ ] 邊界值測試
- [ ] 空值/null 測試
- [ ] 權限驗證測試
- [ ] 參數驗證測試
- [ ] 實體存在性測試
- [ ] 業務規則驗證

---

**最後更新**: 2025-01-08  
**版本**: 1.0


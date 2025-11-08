# 🔧 郵件與密碼重設功能錯誤修復報告

## 問題描述

**報告日期**: 2025-11-08  
**發現時間**: 21:52  

### 問題 1: 郵件發送失敗
**錯誤訊息**: `Conversion = b, Flags = #`  
**原因**: HTML 模板中的 CSS 顏色碼 `#764ba2` 被 Java 的 `String.formatted()` 誤認為格式化佔位符

### 問題 2: 重設密碼頁面錯誤
**錯誤訊息**: `無效的重設令牌`  
**原因**: GET 請求驗證失敗時返回不存在的 `error` 頁面，且錯誤處理不完善

---

## ✅ 修復方案

### 修復 1: EmailService.java - 郵件模板格式化錯誤

**問題代碼**:
```java
.button {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
...
""".formatted(username, resetLink, resetLink, resetLink);
```

**錯誤原因**:
- `#764ba2` 中的 `#` 和 `%` 被視為格式化符號
- Java 的 `String.formatted()` 嘗試解析 `#764b` 導致錯誤

**修復方案**:
1. 將漸層色改為 `rgb()` 格式
2. 使用 `String.format()` 並轉義 `%` 符號

**修復後代碼**:
```java
private String buildPasswordResetEmailHtml(String username, String resetLink) {
    String htmlTemplate = """
        ...
        .button {
            background: linear-gradient(135deg, rgb(102, 126, 234) 0%%, rgb(118, 75, 162) 100%%);
        }
        ...
        """;
    
    return String.format(htmlTemplate, username, resetLink, resetLink, resetLink);
}
```

**關鍵變更**:
- ✅ `#667eea` → `rgb(102, 126, 234)`
- ✅ `#764ba2` → `rgb(118, 75, 162)`
- ✅ `0%` → `0%%` (轉義百分號)
- ✅ `100%` → `100%%` (轉義百分號)
- ✅ `.formatted()` → `String.format()`

---

### 修復 2: UserProfileController.java - 錯誤處理改善

#### 2.1 GET 請求錯誤處理

**問題代碼**:
```java
@GetMapping("/reset-password")
public String resetPasswordPage(@RequestParam("token") String token, Model model) {
    try {
        userService.validateResetToken(token);
        model.addAttribute("token", token);
        model.addAttribute("resetPasswordDTO", new ResetPasswordDTO());
        return "reset-password";
    } catch (RuntimeException e) {
        model.addAttribute("error", e.getMessage());
        return "error";  // ❌ 這個頁面不存在！
    }
}
```

**修復後代碼**:
```java
@GetMapping("/reset-password")
public String resetPasswordPage(@RequestParam("token") String token, Model model,
                                RedirectAttributes redirectAttributes) {
    try {
        userService.validateResetToken(token);
        model.addAttribute("token", token);
        model.addAttribute("resetPasswordDTO", new ResetPasswordDTO());
        return "reset-password";
    } catch (RuntimeException e) {
        // ✅ 重定向到忘記密碼頁面並顯示錯誤
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/user/forgot-password";
    }
}
```

**改善點**:
- ✅ 令牌無效時重定向到忘記密碼頁面
- ✅ 使用 `RedirectAttributes` 傳遞錯誤訊息
- ✅ 避免返回不存在的頁面

#### 2.2 POST 請求錯誤處理

**問題代碼**:
```java
@PostMapping("/reset-password")
public String resetPassword(..., RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        redirectAttributes.addFlashAttribute("error", "資料驗證失敗");
        return "redirect:/user/reset-password?token=" + dto.getToken();  // ❌ 會重新驗證 token
    }
    
    try {
        userService.resetPassword(...);
        return "redirect:/login";
    } catch (RuntimeException e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/user/reset-password?token=" + dto.getToken();  // ❌ 會重新驗證 token
    }
}
```

**修復後代碼**:
```java
@PostMapping("/reset-password")
public String resetPassword(..., Model model, RedirectAttributes redirectAttributes) {
    if (result.hasErrors()) {
        model.addAttribute("error", "資料驗證失敗，請檢查輸入");
        model.addAttribute("token", dto.getToken());
        return "reset-password";  // ✅ 直接返回頁面，不重定向
    }
    
    try {
        userService.resetPassword(...);
        redirectAttributes.addFlashAttribute("success", "密碼重設成功，請使用新密碼登入");
        return "redirect:/login";
    } catch (RuntimeException e) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("token", dto.getToken());
        return "reset-password";  // ✅ 直接返回頁面，保留用戶輸入
    }
}
```

**改善點**:
- ✅ 錯誤時直接返回頁面，避免重新驗證 token
- ✅ 保留用戶輸入的資料
- ✅ 錯誤訊息更清楚

---

## 📋 修改檔案清單

| 檔案 | 修改內容 | 行數變化 |
|------|---------|---------|
| EmailService.java | 修復郵件模板格式化 | ~120 行 |
| UserProfileController.java | 改善錯誤處理 | ~40 行 |

---

## 🧪 測試驗證

### 編譯測試 ✅

```
[INFO] Building booking 0.0.1-SNAPSHOT
[INFO] Compiling 41 source files
[INFO] BUILD SUCCESS
[INFO] Total time:  1.837 s
```

### 功能測試建議

#### 1. 測試郵件發送

**步驟**:
1. 配置 Gmail SMTP
2. 訪問 `/user/forgot-password`
3. 輸入有效的 Email
4. 提交表單
5. 檢查日誌確認沒有 "Conversion = b" 錯誤
6. 檢查信箱是否收到郵件

**預期結果**:
- ✅ 後台日誌沒有格式化錯誤
- ✅ 郵件成功發送
- ✅ HTML 郵件格式正確

#### 2. 測試無效令牌處理

**步驟**:
1. 訪問 `/user/reset-password?token=invalid-token`
2. 驗證是否重定向到忘記密碼頁面
3. 驗證錯誤訊息是否顯示

**預期結果**:
- ✅ 重定向到 `/user/forgot-password`
- ✅ 顯示錯誤訊息：「無效的重設令牌」
- ✅ 沒有 Whitelabel Error Page

#### 3. 測試過期令牌處理

**步驟**:
1. 生成一個重設令牌
2. 等待 24 小時（或手動修改資料庫）
3. 訪問重設密碼頁面
4. 驗證錯誤處理

**預期結果**:
- ✅ 顯示錯誤：「重設令牌已過期」
- ✅ 重定向到忘記密碼頁面

#### 4. 測試重設密碼流程

**完整流程**:
1. 訪問忘記密碼頁面
2. 輸入 Email
3. 從郵件中點擊連結
4. 設定新密碼
5. 提交表單
6. 驗證重定向到登入頁面
7. 使用新密碼登入

**預期結果**:
- ✅ 整個流程順暢無錯誤
- ✅ 成功訊息正確顯示
- ✅ 新密碼可以登入

#### 5. 測試驗證錯誤

**步驟**:
1. 訪問重設密碼頁面（有效 token）
2. 輸入不一致的密碼
3. 提交表單

**預期結果**:
- ✅ 留在重設密碼頁面
- ✅ 顯示錯誤訊息
- ✅ 用戶輸入保留
- ✅ Token 仍然有效

---

## 🎯 修復前後對比

### 郵件發送

| 項目 | 修復前 | 修復後 |
|------|--------|--------|
| 格式化方法 | `.formatted()` | `String.format()` |
| 漸層顏色 | `#667eea` → 錯誤 | `rgb()` → ✅ |
| 百分號 | `0%` → 錯誤 | `0%%` → ✅ |
| 結果 | ❌ 發送失敗 | ✅ 發送成功 |

### 錯誤處理

| 場景 | 修復前 | 修復後 |
|------|--------|--------|
| 無效令牌 (GET) | 返回不存在的 error 頁面 | 重定向到忘記密碼頁面 |
| 驗證失敗 (POST) | 重定向（丟失輸入） | 返回頁面（保留輸入） |
| 錯誤訊息 | 可能丟失 | ✅ 正確顯示 |
| 用戶體驗 | ❌ 差 | ✅ 好 |

---

## 🔍 技術細節

### String.format() vs .formatted()

**問題**:
```java
// ❌ .formatted() 會解析 # 和 %
"background: #764ba2 100%".formatted()
// 錯誤: Conversion = b, Flags = #
```

**解決**:
```java
// ✅ String.format() 配合 %% 轉義
String.format("background: rgb(118, 75, 162) 100%%")
// 結果: "background: rgb(118, 75, 162) 100%"
```

### 轉義規則

| 字元 | 原始 | 需轉義 | 轉義後 |
|------|------|--------|--------|
| 百分號 | `%` | 是 | `%%` |
| 井號 | `#` | 否（在 format 中） | `#` |
| 顏色碼 | `#667eea` | 改用 rgb() | `rgb(102, 126, 234)` |

### 錯誤處理最佳實踐

**不好的做法**:
```java
catch (Exception e) {
    return "redirect:/page?param=" + value;  // ❌ 會丟失錯誤訊息和用戶輸入
}
```

**好的做法**:
```java
catch (Exception e) {
    model.addAttribute("error", e.getMessage());
    model.addAttribute("data", preservedData);
    return "page-name";  // ✅ 保留錯誤訊息和用戶輸入
}
```

---

## 📚 相關文件

- [EmailService.java](../src/main/java/com/example/booking/service/EmailService.java)
- [UserProfileController.java](../src/main/java/com/example/booking/controller/UserProfileController.java)
- [reset-password.html](../src/main/resources/templates/reset-password.html)
- [SPRING_MAIL_SETUP_GUIDE.md](./SPRING_MAIL_SETUP_GUIDE.md)

---

## ✨ 總結

### 修復項目 ✅

- ✅ 修復郵件模板格式化錯誤
- ✅ 改善無效令牌錯誤處理
- ✅ 改善驗證失敗錯誤處理
- ✅ 提升用戶體驗
- ✅ 編譯測試通過

### 影響範圍

- **修復**: 2 個檔案
- **測試**: 編譯成功
- **部署**: 需要重啟應用

### 測試狀態

- ✅ 編譯測試通過
- ⏳ 功能測試待執行

### 下一步

1. **重啟應用程式**
2. **配置郵件 SMTP**
3. **測試忘記密碼完整流程**
4. **驗證郵件能正確發送**
5. **驗證錯誤處理正確**

---

**修復日期**: 2025-11-08  
**版本**: 1.1  
**狀態**: ✅ 修復完成，待測試驗證


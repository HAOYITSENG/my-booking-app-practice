# 🔧 郵件發送認證失敗問題解決方案

## 📋 問題總結

**日期**: 2025-11-08  
**時間**: 21:59  
**錯誤**: Authentication failed  
**原因**: Gmail SMTP 認證失敗

---

## 🔍 錯誤日誌分析

```
2025-11-08T21:59:10.402+08:00 ERROR 52736 --- [nio-8080-exec-5] c.example.booking.service.EmailService   : 發送郵件時發生錯誤: Authentication failed
2025-11-08T21:59:10.402+08:00 ERROR 52736 --- [nio-8080-exec-5] c.example.booking.service.UserService    : 發送密碼重設郵件失敗: 郵件發送失敗
2025-11-08T21:59:10.402+08:00  INFO 52736 --- [nio-8080-exec-5] c.example.booking.service.UserService    : 重設連結（備用）：http://localhost:8080/user/reset-password?token=cb965a98-3a69-49cd-912a-d3bc14119932
```

**關鍵資訊**:
- ✅ 令牌生成成功
- ✅ 資料庫更新成功
- ❌ SMTP 認證失敗
- ✅ 備用連結已生成（可用於測試）

---

## ✅ 解決方案

### 立即可用的臨時方案

**使用備用連結直接測試重設密碼功能**:

從日誌中複製連結：
```
http://localhost:8080/user/reset-password?token=cb965a98-3a69-49cd-912a-d3bc14119932
```

1. 複製上面的連結
2. 在瀏覽器中打開
3. 設定新密碼
4. 測試重設密碼流程

**優點**: 可以立即測試功能，不需要等待郵件設定

---

### 完整解決方案：設定 Gmail SMTP

請按照以下步驟設定 Gmail 應用程式密碼：

#### 步驟 1: 啟用兩步驟驗證

1. 前往 https://myaccount.google.com/security
2. 點擊「兩步驟驗證」
3. 按照指示完成設定

#### 步驟 2: 生成應用程式密碼

1. 前往 https://myaccount.google.com/apppasswords
2. 選擇「郵件」和「其他（自訂名稱）」
3. 輸入「訂房系統」
4. 點擊「產生」
5. 複製顯示的 16 位密碼（移除空格）

#### 步驟 3: 更新配置

編輯 `src/main/resources/application.properties`:

```properties
spring.mail.username=howie960018@gmail.com
spring.mail.password=你的16位應用程式密碼（無空格）
app.mail.from=howie960018@gmail.com
```

#### 步驟 4: 重啟應用程式

停止並重新啟動應用程式以載入新配置。

---

## 📚 詳細設定指南

完整的設定步驟請參考：
**[GMAIL_SMTP_SETUP_GUIDE.md](./GMAIL_SMTP_SETUP_GUIDE.md)**

包含內容：
- 📋 詳細的圖文設定步驟
- 🐛 常見問題排解
- 🔒 安全性建議
- 🧪 測試驗證方法

---

## 🎯 設定檢查清單

在設定郵件功能前，請確認：

- [ ] **Gmail 兩步驟驗證**
  - 已啟用兩步驟驗證
  - 可以正常接收驗證碼

- [ ] **應用程式密碼**
  - 已生成應用程式密碼
  - 已複製 16 位密碼（無空格）

- [ ] **application.properties**
  - `spring.mail.username` 設為您的 Gmail
  - `spring.mail.password` 設為應用程式密碼
  - `app.mail.from` 設為您的 Gmail

- [ ] **應用程式重啟**
  - 已停止舊的程序
  - 已重新啟動應用程式

---

## 🧪 測試步驟

### 方法 1: 使用備用連結（立即測試）

```
1. 複製備用連結
2. 在瀏覽器中打開
3. 設定新密碼
4. 提交表單
5. 使用新密碼登入
```

### 方法 2: 完整郵件流程（設定後測試）

```
1. 訪問 /user/forgot-password
2. 輸入 howie960018@gmail.com
3. 提交表單
4. 檢查 Gmail 信箱（包括垃圾郵件）
5. 點擊郵件中的連結
6. 設定新密碼
```

---

## 🔍 驗證設定是否成功

### 成功的日誌

設定正確後，應該看到：

```
INFO  c.e.b.service.UserService     : 為電子郵件 howie960018@gmail.com 生成密碼重設令牌
INFO  c.e.b.service.UserService     : 用戶 user1 的密碼重設令牌已生成
INFO  c.e.b.service.UserService     : 發送密碼重設郵件至 howie960018@gmail.com
INFO  c.e.b.service.EmailService    : HTML 郵件已發送至 howie960018@gmail.com
INFO  c.e.b.service.EmailService    : 密碼重設郵件已發送至 howie960018@gmail.com (用戶: user1)
INFO  c.e.b.service.UserService     : 密碼重設郵件發送成功至 howie960018@gmail.com
```

**關鍵指標**:
- ✅ 沒有 ERROR 日誌
- ✅ 看到「HTML 郵件已發送」
- ✅ 看到「密碼重設郵件已發送」
- ✅ 看到「密碼重設郵件發送成功」

---

## 🐛 常見錯誤及解決方案

### 錯誤 1: Authentication failed

**原因**: 
- 使用了 Gmail 登入密碼而非應用程式密碼
- 應用程式密碼複製錯誤（包含空格）
- 兩步驟驗證未啟用

**解決**:
```properties
# ❌ 錯誤
spring.mail.password=your-gmail-password

# ✅ 正確
spring.mail.password=abcdefghijklmnop
```

### 錯誤 2: Username and Password not accepted

**原因**: 密碼錯誤

**解決**: 重新生成應用程式密碼

### 錯誤 3: Connection timed out

**原因**: 網路或防火牆問題

**解決**: 
- 確認端口 587 未被阻擋
- 檢查防火牆設定

---

## 🔒 安全性提醒

### ⚠️ 重要

1. **不要將應用程式密碼提交到 Git**
   ```bash
   # 將 application.properties 加入 .gitignore
   echo "src/main/resources/application.properties" >> .gitignore
   ```

2. **使用環境變數（生產環境）**
   ```properties
   spring.mail.password=${MAIL_PASSWORD:default-for-dev}
   ```

3. **定期更換密碼**
   - 每 3-6 個月更換一次
   - 發現異常時立即更換

---

## 📊 目前狀態

| 項目 | 狀態 | 說明 |
|------|------|------|
| 令牌生成 | ✅ 正常 | 可以生成重設令牌 |
| 資料庫更新 | ✅ 正常 | 令牌已儲存到資料庫 |
| 郵件發送 | ❌ 失敗 | SMTP 認證失敗 |
| 備用連結 | ✅ 可用 | 可用於測試功能 |
| 重設密碼功能 | ✅ 可用 | 使用備用連結測試 |

---

## 🎯 後續步驟

### 立即執行

1. **使用備用連結測試重設密碼功能**
   ```
   http://localhost:8080/user/reset-password?token=cb965a98-3a69-49cd-912a-d3bc14119932
   ```

2. **驗證功能正常運作**
   - 設定新密碼
   - 使用新密碼登入

### 稍後執行（設定郵件）

1. **按照 GMAIL_SMTP_SETUP_GUIDE.md 設定**
2. **生成 Gmail 應用程式密碼**
3. **更新 application.properties**
4. **重啟應用程式**
5. **測試完整郵件流程**

---

## 📞 需要協助？

如果按照指南操作後仍有問題：

1. **檢查日誌**
   - 複製完整的錯誤訊息
   - 特別注意 `EmailService` 的日誌

2. **驗證設定**
   ```properties
   # 檢查這三個設定是否正確
   spring.mail.username=你的Gmail
   spring.mail.password=16位應用程式密碼
   app.mail.from=你的Gmail
   ```

3. **測試連線**
   - 確認可以訪問 Gmail
   - 確認兩步驟驗證已啟用

---

**文件建立日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ⏳ 待設定 Gmail SMTP


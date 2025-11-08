# 📧 Gmail SMTP 設定步驟指南

## ⚠️ 目前問題

**錯誤訊息**: `Authentication failed`  
**原因**: 尚未設定正確的 Gmail 帳號和應用程式密碼

---

## 🔧 完整設定步驟

### 步驟 1: 啟用 Gmail 兩步驟驗證

1. **登入 Google 帳號**
   - 前往：https://myaccount.google.com/

2. **進入安全性設定**
   - 點擊左側選單的「安全性」
   - 或直接訪問：https://myaccount.google.com/security

3. **啟用兩步驟驗證**
   - 找到「登入 Google」區塊
   - 點擊「兩步驟驗證」
   - 如果尚未啟用，點擊「開始使用」
   - 按照指示完成設定（通常需要手機號碼）

---

### 步驟 2: 生成應用程式密碼

1. **返回安全性頁面**
   - 確認兩步驟驗證已啟用

2. **找到應用程式密碼選項**
   - 在「登入 Google」區塊中
   - 點擊「應用程式密碼」
   - 如果找不到，直接訪問：https://myaccount.google.com/apppasswords

3. **生成新密碼**
   - 選擇應用程式：選擇「郵件」
   - 選擇裝置：選擇「其他（自訂名稱）」
   - 輸入名稱：例如「訂房系統」
   - 點擊「產生」

4. **複製應用程式密碼**
   - Google 會顯示一個 16 位數的密碼（例如：`abcd efgh ijkl mnop`）
   - **重要**：複製此密碼（移除空格：`abcdefghijklmnop`）
   - 此密碼只會顯示一次，請妥善保存

---

### 步驟 3: 更新 application.properties

開啟檔案：`src/main/resources/application.properties`

找到以下區塊並修改：

```properties
# ===== Spring Mail Configuration =====
# Gmail SMTP 配置
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=howie960018@gmail.com
spring.mail.password=abcdefghijklmnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

# 郵件寄件者資訊
app.mail.from=howie960018@gmail.com
app.mail.from-name=訂房系統

# 重設密碼連結的基礎 URL
app.base-url=http://localhost:8080
```

**替換內容**:
- `spring.mail.username=howie960018@gmail.com` ← 您的 Gmail 地址
- `spring.mail.password=abcdefghijklmnop` ← 步驟 2 生成的應用程式密碼（移除空格）
- `app.mail.from=howie960018@gmail.com` ← 您的 Gmail 地址

---

### 步驟 4: 重啟應用程式

**使用 IDE (IntelliJ/Eclipse)**:
1. 停止當前運行的應用程式（紅色停止按鈕）
2. 重新運行 `BookingApplication.main()`

**或使用 Maven**:
```bash
mvn spring-boot:run
```

---

### 步驟 5: 測試郵件功能

1. **訪問忘記密碼頁面**
   ```
   http://localhost:8080/user/forgot-password
   ```

2. **輸入 Email**
   - 輸入您在 `data.sql` 中設定的 Email
   - 例如：`howie960018@gmail.com`

3. **檢查結果**
   - 檢查後台日誌，應該看到：
     ```
     INFO  c.e.b.service.EmailService : HTML 郵件已發送至 howie960018@gmail.com
     INFO  c.e.b.service.EmailService : 密碼重設郵件已發送至 howie960018@gmail.com
     INFO  c.e.b.service.UserService  : 密碼重設郵件發送成功至 howie960018@gmail.com
     ```
   - 檢查您的 Gmail 信箱（包括垃圾郵件）

---

## 🐛 常見問題排解

### 問題 1: 仍然顯示 "Authentication failed"

**可能原因**:
1. 應用程式密碼複製錯誤（包含空格）
2. 使用了 Gmail 登入密碼而非應用程式密碼
3. 兩步驟驗證未正確啟用

**解決方案**:
- 重新生成應用程式密碼
- 確認密碼沒有空格（16位連續字元）
- 確認兩步驟驗證已啟用

### 問題 2: "Username and Password not accepted"

**原因**: 使用了錯誤的密碼

**解決方案**:
- 必須使用應用程式密碼，不是 Gmail 登入密碼
- 重新檢查 `spring.mail.password` 的值

### 問題 3: 郵件進入垃圾郵件

**解決方案**:
- 檢查垃圾郵件資料夾
- 將寄件者加入聯絡人
- 標記為「不是垃圾郵件」

### 問題 4: "Connection timed out"

**可能原因**:
- 防火牆阻擋
- 網路問題
- SMTP 端口錯誤

**解決方案**:
- 確認端口是 587（TLS）
- 檢查防火牆設定
- 嘗試使用其他網路

---

## 📝 設定檢查清單

完成設定前請確認：

- [ ] Gmail 兩步驟驗證已啟用
- [ ] 已生成應用程式密碼
- [ ] 應用程式密碼已複製（無空格）
- [ ] `application.properties` 已更新
  - [ ] `spring.mail.username` 設為您的 Gmail
  - [ ] `spring.mail.password` 設為應用程式密碼
  - [ ] `app.mail.from` 設為您的 Gmail
- [ ] 應用程式已重啟
- [ ] 測試郵件發送功能

---

## 🔒 安全性注意事項

### ⚠️ 不要將密碼提交到 Git

**方法 1: 使用 .gitignore**
```bash
# 將 application.properties 加入 .gitignore
echo "src/main/resources/application.properties" >> .gitignore
```

**方法 2: 建立範本檔案**
1. 創建 `application.properties.template`（提交到 Git）
2. 將 `application.properties` 加入 `.gitignore`
3. 團隊成員複製範本並填入自己的設定

**方法 3: 使用環境變數（生產環境建議）**

修改 `application.properties`:
```properties
spring.mail.username=${MAIL_USERNAME:your-email@gmail.com}
spring.mail.password=${MAIL_PASSWORD:your-app-password}
```

設定環境變數（Windows）:
```cmd
setx MAIL_USERNAME "howie960018@gmail.com"
setx MAIL_PASSWORD "abcdefghijklmnop"
```

---

## 📊 測試驗證

### 成功的日誌應該顯示：

```
INFO  c.e.b.service.UserService     : 為電子郵件 xxx@gmail.com 生成密碼重設令牌
INFO  c.e.b.service.UserService     : 用戶 xxx 的密碼重設令牌已生成
INFO  c.e.b.service.UserService     : 發送密碼重設郵件至 xxx@gmail.com
INFO  c.e.b.service.EmailService    : HTML 郵件已發送至 xxx@gmail.com
INFO  c.e.b.service.EmailService    : 密碼重設郵件已發送至 xxx@gmail.com
INFO  c.e.b.service.UserService     : 密碼重設郵件發送成功至 xxx@gmail.com
```

### 失敗的日誌（需修復）：

```
ERROR c.e.b.service.EmailService    : 發送郵件時發生錯誤: Authentication failed
ERROR c.e.b.service.UserService     : 發送密碼重設郵件失敗: 郵件發送失敗
INFO  c.e.b.service.UserService     : 重設連結（備用）：http://localhost:8080/...
```

---

## 🎯 快速設定（懶人包）

1. **前往** https://myaccount.google.com/apppasswords
2. **生成**應用程式密碼
3. **複製**16位密碼（移除空格）
4. **編輯** `application.properties`:
   ```properties
   spring.mail.username=你的Gmail
   spring.mail.password=16位應用程式密碼
   app.mail.from=你的Gmail
   ```
5. **重啟**應用程式
6. **測試**忘記密碼功能

---

## 📞 需要協助？

如果仍然遇到問題：

1. **檢查日誌**
   - 複製完整的錯誤訊息
   - 特別注意 `EmailService` 和 `UserService` 的日誌

2. **驗證設定**
   - 確認應用程式密碼正確
   - 確認沒有多餘的空格

3. **測試連線**
   - 確認可以訪問 `smtp.gmail.com:587`
   - 檢查防火牆設定

---

**文件建立日期**: 2025-11-08  
**版本**: 1.0  
**適用於**: Gmail SMTP 設定


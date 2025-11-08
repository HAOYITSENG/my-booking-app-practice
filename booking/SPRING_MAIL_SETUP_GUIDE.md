# 📧 Spring Mail 郵件服務設定指南

## 📋 實作概述

**完成日期**: 2025-11-08  
**功能**: 整合真實的郵件發送服務  
**使用技術**: Spring Boot Mail, JavaMailSender  

---

## ✅ 已完成項目

### 1. 依賴配置 ✅
- 添加 `spring-boot-starter-mail` 依賴
- 版本由 Spring Boot 自動管理

### 2. 郵件服務類 ✅
- `EmailService.java` - 提供各種郵件發送功能
- 支援純文字和 HTML 郵件
- 預設郵件模板

### 3. 配置檔案 ✅
- `application.properties` - 郵件伺服器配置
- `MailConfig.java` - Java 配置類（可選）

### 4. 整合到現有功能 ✅
- 忘記密碼功能使用真實郵件發送

---

## 🔧 設定步驟

### 方案一：使用 Gmail（推薦測試用）

#### 1. 啟用 Gmail 的應用程式密碼

**步驟**:
1. 登入您的 Google 帳號
2. 前往 [Google 帳戶安全性設定](https://myaccount.google.com/security)
3. 啟用「兩步驟驗證」（如果尚未啟用）
4. 在「兩步驟驗證」頁面，找到「應用程式密碼」
5. 選擇「郵件」和「其他裝置」
6. 生成應用程式密碼（16位數字，例如：`abcd efgh ijkl mnop`）

#### 2. 更新 application.properties

```properties
# Gmail SMTP 配置
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=abcdefghijklmnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# 郵件寄件者資訊
app.mail.from=your-email@gmail.com
app.mail.from-name=訂房系統

# 重設密碼連結的基礎 URL
app.base-url=http://localhost:8080
```

**注意事項**:
- ✅ 使用「應用程式密碼」，不是 Gmail 登入密碼
- ✅ 密碼中的空格要移除（`abcd efgh` → `abcdefgh`）
- ✅ 確保啟用了兩步驟驗證

---

### 方案二：使用 Outlook/Hotmail

```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=your-email@outlook.com
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

### 方案三：使用 Yahoo Mail

```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=your-email@yahoo.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

### 方案四：使用自定義 SMTP 伺服器

```properties
spring.mail.host=smtp.your-domain.com
spring.mail.port=587
spring.mail.username=your-username
spring.mail.password=your-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 📝 EmailService 功能說明

### 已實作的方法

| 方法 | 功能 | 使用場景 |
|------|------|---------|
| `sendSimpleEmail()` | 發送純文字郵件 | 簡單通知 |
| `sendHtmlEmail()` | 發送 HTML 郵件 | 格式化內容 |
| `sendPasswordResetEmail()` | 發送密碼重設郵件 | 忘記密碼 |
| `sendWelcomeEmail()` | 發送歡迎郵件 | 註冊成功 |
| `sendBookingConfirmationEmail()` | 發送訂單確認郵件 | 訂房成功 |

### 使用範例

#### 1. 發送密碼重設郵件

```java
@Autowired
private EmailService emailService;

public void handleForgotPassword(String email) {
    String resetToken = generateResetToken(email);
    emailService.sendPasswordResetEmail(email, username, resetToken);
}
```

#### 2. 發送歡迎郵件

```java
@Autowired
private EmailService emailService;

public void registerUser(RegisterDTO dto) {
    // ... 註冊邏輯 ...
    emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
}
```

#### 3. 發送訂單確認郵件

```java
@Autowired
private EmailService emailService;

public void createBooking(BookingDTO dto) {
    // ... 建立訂單 ...
    emailService.sendBookingConfirmationEmail(
        user.getEmail(),
        user.getUsername(),
        "台北商旅",
        "2025-12-01",
        "2025-12-03",
        "4400"
    );
}
```

---

## 🎨 郵件模板設計

### 密碼重設郵件模板

**特點**:
- ✅ 漂亮的 HTML 排版
- ✅ 漸層色彩設計
- ✅ 清晰的 CTA 按鈕
- ✅ 備用連結（複製貼上用）
- ✅ 安全提示
- ✅ 有效期限說明

**效果預覽**:
```
┌─────────────────────────────────┐
│     🏨 訂房系統                  │
│     密碼重設通知                  │
├─────────────────────────────────┤
│ 親愛的 user，您好：              │
│                                 │
│ 我們收到了您的密碼重設請求。      │
│                                 │
│     [🔒 重設密碼]  <-- 按鈕      │
│                                 │
│ 📋 如果按鈕無法點擊...           │
│ http://localhost:8080/...       │
│                                 │
│ ⚠️ 重要提示：                   │
│ • 連結將在 24 小時後失效          │
│ • 如果您沒有請求重設...           │
└─────────────────────────────────┘
```

---

## 🧪 測試郵件功能

### 1. 單元測試（手動測試）

創建測試控制器（僅用於測試）:

```java
@RestController
@RequestMapping("/api/test")
public class EmailTestController {
    
    @Autowired
    private EmailService emailService;
    
    @GetMapping("/send-test-email")
    public String sendTestEmail(@RequestParam String to) {
        emailService.sendSimpleEmail(to, "測試郵件", "這是一封測試郵件");
        return "郵件已發送至：" + to;
    }
}
```

**測試**:
```
訪問: http://localhost:8080/api/test/send-test-email?to=your-email@gmail.com
```

### 2. 測試密碼重設流程

**步驟**:
1. 訪問 `/user/forgot-password`
2. 輸入註冊的 Email
3. 提交表單
4. 檢查信箱（包括垃圾郵件）
5. 點擊郵件中的「重設密碼」按鈕
6. 設定新密碼
7. 使用新密碼登入

---

## 🐛 常見問題排解

### 問題 1: 郵件發送失敗

**錯誤訊息**: `AuthenticationFailedException`

**解決方案**:
- 檢查 Gmail 是否啟用「應用程式密碼」
- 確認密碼沒有空格
- 確認帳號有啟用兩步驟驗證

### 問題 2: 連接超時

**錯誤訊息**: `MailConnectException: Couldn't connect to host`

**解決方案**:
- 檢查防火牆設定
- 確認 SMTP 端口（587 或 465）
- 確認網路連線

### 問題 3: 郵件進入垃圾郵件

**解決方案**:
- 使用正式的寄件者名稱
- 添加 SPF、DKIM 記錄（生產環境）
- 避免使用垃圾郵件關鍵字

### 問題 4: HTML 郵件格式跑掉

**解決方案**:
- 使用內聯 CSS（`style=""` 而非 `<style>`）
- 測試不同郵件客戶端
- 使用郵件模板工具

---

## 🔒 安全性建議

### 1. 不要將密碼寫在程式碼中

❌ **錯誤**:
```properties
spring.mail.password=my-actual-password
```

✅ **正確**:
```properties
spring.mail.password=${MAIL_PASSWORD}
```

然後設定環境變數：
```bash
export MAIL_PASSWORD=your-app-password
```

### 2. 使用應用程式密碼

- ✅ 不要使用帳號主密碼
- ✅ 為每個應用程式生成獨立密碼
- ✅ 定期更換密碼

### 3. 限制郵件發送頻率

```java
// 防止郵件轟炸
private Map<String, LocalDateTime> lastSentTime = new ConcurrentHashMap<>();

public void sendEmail(String to, String subject, String content) {
    LocalDateTime last = lastSentTime.get(to);
    if (last != null && Duration.between(last, LocalDateTime.now()).toMinutes() < 5) {
        throw new RuntimeException("請稍後再試（5分鐘內只能發送一次）");
    }
    
    // ... 發送郵件 ...
    lastSentTime.put(to, LocalDateTime.now());
}
```

---

## 📊 生產環境建議

### 1. 使用專業郵件服務

**推薦服務**:
- **SendGrid** - 免費額度 100 封/天
- **Mailgun** - 免費額度 1000 封/月
- **Amazon SES** - 按使用量計費
- **Mailjet** - 免費額度 200 封/天

### 2. SendGrid 整合範例

**依賴**:
```xml
<dependency>
    <groupId>com.sendgrid</groupId>
    <artifactId>sendgrid-java</artifactId>
    <version>4.9.3</version>
</dependency>
```

**配置**:
```properties
sendgrid.api-key=your-sendgrid-api-key
```

**使用**:
```java
@Service
public class SendGridEmailService {
    
    @Value("${sendgrid.api-key}")
    private String apiKey;
    
    public void sendEmail(String to, String subject, String content) {
        Email from = new Email("noreply@yourdomain.com");
        Email toEmail = new Email(to);
        Content emailContent = new Content("text/html", content);
        Mail mail = new Mail(from, subject, toEmail, emailContent);
        
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        
        sg.api(request);
    }
}
```

### 3. 郵件佇列

**高流量時使用訊息佇列**:
- 使用 RabbitMQ 或 Redis Queue
- 非同步發送郵件
- 失敗重試機制

---

## 🎯 測試清單

### 功能測試

- [ ] Gmail 帳號配置完成
- [ ] 發送測試郵件成功
- [ ] 忘記密碼郵件發送成功
- [ ] 郵件內容格式正確
- [ ] 重設連結可以點擊
- [ ] 重設密碼流程完整

### 錯誤處理測試

- [ ] 無效 Email 處理
- [ ] SMTP 連接失敗處理
- [ ] 認證失敗處理
- [ ] 郵件發送超時處理

### 安全性測試

- [ ] 令牌過期驗證
- [ ] 無效令牌處理
- [ ] 頻率限制測試

---

## 📚 相關文件

- [UserService](../src/main/java/com/example/booking/service/UserService.java)
- [EmailService](../src/main/java/com/example/booking/service/EmailService.java)
- [MailConfig](../src/main/java/com/example/booking/config/MailConfig.java)
- [忘記密碼頁面](../src/main/resources/templates/forgot-password.html)

---

## ✨ 總結

### 完成項目 ✅

- ✅ Spring Mail 依賴配置
- ✅ EmailService 實作
- ✅ HTML 郵件模板
- ✅ 密碼重設郵件整合
- ✅ 錯誤處理機制
- ✅ 配置文件設定

### 待實作（可選）

- [ ] 歡迎郵件整合到註冊流程
- [ ] 訂單確認郵件整合到訂房流程
- [ ] 郵件發送頻率限制
- [ ] 郵件佇列系統
- [ ] 生產環境郵件服務整合

### 使用方式

1. **設定 Gmail 應用程式密碼**
2. **更新 application.properties**
3. **重啟應用程式**
4. **測試忘記密碼功能**

---

**文件建立日期**: 2025-11-08  
**版本**: 1.0  
**狀態**: ✅ 郵件服務已完整實作


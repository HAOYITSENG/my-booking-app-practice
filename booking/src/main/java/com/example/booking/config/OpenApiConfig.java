package com.example.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Booking Management System API")
                        .version("v1.0.0")
                        .description("""
                                # 完整的訂房系統 RESTful API 文件

                                ## 🎯 系統概述
                                這是一個功能完整的訂房管理系統，提供住宿查詢、房型管理、訂單處理、
                                統計分析、資料匯出等功能。

                                ## 🔐 認證方式
                                本 API 使用 **Session-based 認證**。使用前請先透過 Web 介面登入：
                                1. 訪問 http://localhost:8080/login
                                2. 使用測試帳號登入
                                3. 系統會自動建立 Session Cookie (JSESSIONID)
                                4. 後續 API 請求會自動攜帶此 Cookie

                                ## 👥 測試帳號
                                | 角色 | 帳號 | 密碼 | 權限說明 |
                                |------|------|------|----------|
                                | 管理員 | admin | admin123 | 完整系統管理權限 |
                                | 房東 | owner | owner123 | 管理自己的住宿與訂單 |
                                | 一般用戶 | user | user123 | 查詢與預訂功能 |

                                ## 📋 API 分組說明
                                - **Authentication**: 使用者註冊與認證
                                - **Accommodations**: 住宿資訊查詢（公開）
                                - **Room Types**: 房型資訊查詢（公開）
                                - **Bookings**: 訂單管理（需登入）
                                - **Statistics**: 統計資料（需登入）
                                - **Export**: 資料匯出（需登入）

                                ## 🚀 快速開始
                                1. 使用 `GET /api/accommodations` 查看所有住宿
                                2. 使用 `GET /api/room-types/by-accommodation/{id}` 查看房型
                                3. 登入後使用 `POST /api/bookings/by-room-type` 建立訂單
                                4. 使用 `GET /api/bookings` 查看自己的訂單

                                ## 📊 主要功能
                                - ✅ 住宿與房型管理
                                - ✅ 庫存管理與衝突檢查
                                - ✅ 訂單建立、確認、取消
                                - ✅ 多角色權限控管
                                - ✅ 統計圖表資料
                                - ✅ Excel 報表匯出

                                ## 🛠️ 技術架構
                                - **後端框架**: Spring Boot 3.2.5
                                - **安全框架**: Spring Security
                                - **資料庫**: MySQL + JPA/Hibernate
                                - **API 文件**: SpringDoc OpenAPI 3
                                """)
                        .contact(new Contact()
                                .name("Developer")
                                .email("developer@bookingsystem.com")
                                .url("https://github.com/yourusername/booking-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("開發環境 (Local)")
                ))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("""
                                        Session-based 認證。

                                        使用步驟：
                                        1. 先透過瀏覽器訪問 /login 頁面登入
                                        2. 登入後系統會建立 Session Cookie
                                        3. 在 Swagger UI 中的 API 請求會自動攜帶 Cookie

                                        注意：直接在 Swagger UI 中測試需要先在同個瀏覽器登入系統。
                                        """)));
    }
}


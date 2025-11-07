package com.example.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 允許 Swagger/OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        // 管理員專用 API 端點
                        .requestMatchers("/api/admin/**", "/api/bookings/admin/**").hasRole("ADMIN")
                        // 房東專用頁面和API端點
                        .requestMatchers("/owner-dashboard", "/owner-accommodations", "/owner-bookings", "/room-type-management", "/api/owner/**").hasRole("OWNER")
                        // 一般用戶頁面
                        .requestMatchers("/user-bookings").authenticated()
                        // 允許靜態資源和認證相關端點
                        .requestMatchers("/h2-console/**", "/login", "/register", "/css/**", "/js/**", "/images/**","/api/auth/**").permitAll()
                        // 允許公開訪問的 API
                        .requestMatchers("/api/accommodations/**").permitAll()
                        // 匯出功能權限設定
                        .requestMatchers("/api/export/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/export/owner/**").hasRole("OWNER")
                        .requestMatchers("/api/export/bookings").authenticated()
                        // 統計功能權限設定
                        .requestMatchers("/api/statistics/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/statistics/owner/**").hasRole("OWNER")
                        .requestMatchers("/api/statistics/**").authenticated()
                        // 其他所有請求需要登入
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            // 輸出調試資訊
                            System.out.println("登入成功，用戶：" + authentication.getName());
                            System.out.println("擁有權限：" + authentication.getAuthorities());

                            if (authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                                System.out.println("導向到管理員儀表板");
                                response.sendRedirect("/admin-dashboard");
                            } else if (authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"))) {
                                System.out.println("導向到房東儀表板");
                                response.sendRedirect("/owner-dashboard");
                            } else {
                                System.out.println("導向到首頁");
                                response.sendRedirect("/");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**", "/api/**"));

        return http.build();
    }
}
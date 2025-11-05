package com.example.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
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
                        // 管理員專用 API 端點
                        .requestMatchers("/api/admin/**", "/api/bookings/admin/**").hasRole("ADMIN")
                        // 允許靜態資源和認證相關端點
                        .requestMatchers("/h2-console/**", "/login", "/register", "/css/**", "/js/**", "/images/**","/api/auth/**").permitAll()
                        // 允許公開訪問的 API
                        .requestMatchers("/api/accommodations/**").permitAll()
                        // 其他 API 端點需要用戶或管理員權限
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                        // 其他所有請求需要登入
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            response.sendRedirect(isAdmin ? "/admin-dashboard" : "/");
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .headers(headers -> headers
                        .frameOptions().sameOrigin()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // 只忽略 H2 console
                ); // 移除 disable()，啟用 CSRF 保護

        return http.build();
    }
}
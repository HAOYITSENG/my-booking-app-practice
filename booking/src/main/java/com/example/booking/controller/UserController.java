package com.example.booking.controller;

import com.example.booking.model.User;
import com.example.booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 註冊新帳號
     * 測試用範例：
     * POST /api/auth/register
     * Content-Type: application/x-www-form-urlencoded
     * username=testuser&password=123456
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String password) {

        // 基本验证
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("帳號與密碼不得為空");
        }

        // 长度验证
        if (username.length() < 3 || username.length() > 20) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("帳號長度需要在3到20個字元之間");
        }
        if (password.length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("密碼至少需要6個字元");
        }

        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("此帳號已被使用");
        }

        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password)); // 加密
            user.setRole("ROLE_USER"); // 确保角色前缀正确

            userRepository.save(user);
            return ResponseEntity.ok("註冊成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("註冊失敗：系統錯誤");
        }
    }

    /**
     * 取得目前登入者資訊
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> currentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("未登入");
        }
        return ResponseEntity.ok(authentication.getName());
    }



}

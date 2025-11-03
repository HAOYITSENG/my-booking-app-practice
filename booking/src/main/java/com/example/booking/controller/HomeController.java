package com.example.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "帳號或密碼錯誤！");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "已成功登出！");
        }

        return "login";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboardPage() {
        return "admin-dashboard";
    }

    @GetMapping("/register") public String registerPage() { return "register"; }
    @GetMapping("/admin-bookings") public String adminBookingsPage() { return "admin-bookings"; }
    @GetMapping("/admin-accommodations") public String adminAccommodationsPage() { return "admin-accommodations"; }

}
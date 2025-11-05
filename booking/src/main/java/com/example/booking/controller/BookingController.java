package com.example.booking.controller;

import com.example.booking.model.Booking;
import com.example.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // === 1. 舊版：以住宿 ID 下單 ===
    @PostMapping
    public ResponseEntity<Booking> bookByAccommodation(
            @RequestParam Long accommodationId,
            @RequestParam String checkIn,
            @RequestParam String checkOut
    ) {
        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);
        Booking booking = bookingService.book(accommodationId, in, out);
        return ResponseEntity.ok(booking);
    }

    // === 2. 新版：以房型 ID 下單（支援數量與庫存檢查）===
    @PostMapping("/by-room-type")
    public ResponseEntity<Booking> bookByRoomType(
            @RequestParam Long roomTypeId,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam(defaultValue = "1") Integer quantity
    ) {
        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);
        Booking booking = bookingService.bookByRoomType(roomTypeId, in, out, quantity);
        return ResponseEntity.ok(booking);
    }

    // === 3. 使用者查自己的訂單（自動取登入帳號） ===
    @GetMapping
    public ResponseEntity<List<Booking>> getMyBookings(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(bookingService.getBookingsForUser(username));
    }

    // === 4. 管理員查所有訂單 ===
    @GetMapping("/admin/all")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // === 5.（選用）Admin 可查指定使用者的訂單 ===
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Booking>> getBookingsForUser(@PathVariable String username) {
        return ResponseEntity.ok(bookingService.getBookingsForUser(username));
    }
}

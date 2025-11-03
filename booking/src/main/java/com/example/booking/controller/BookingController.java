package com.example.booking.controller;

import java.time.LocalDate;
import java.util.List;

import com.example.booking.model.Booking;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.BookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService,
                             BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    // --- 使用者功能 ---

    /** 查詢目前登入者的所有訂房紀錄 */
    @GetMapping
    public List<Booking> myBookings(Authentication auth) {
        return bookingService.getBookingsForUser(auth.getName());
    }

    /** 建立新訂單 */
    @PostMapping
    public ResponseEntity<?> create(@RequestParam("accommodationId") long accommodationId,
                                    @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                                    @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                                    Authentication auth) {

        if (checkIn.isAfter(checkOut)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("入住日期不能晚於退房日期");
        }

        Booking booking = bookingService.book(accommodationId, checkIn, checkOut);
        return ResponseEntity.ok(booking);
    }

    // --- 管理員功能 ---

    /** 查詢所有使用者的訂單 */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllBookings(Authentication authentication) {

        return ResponseEntity.ok(bookingRepository.findAll());
    }

    /** 刪除指定訂單 */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id,
                                           Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("無權限");
        }

        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("找不到指定的訂單");
        }

        bookingRepository.deleteById(id);
        return ResponseEntity.ok("訂單已刪除");
    }
}

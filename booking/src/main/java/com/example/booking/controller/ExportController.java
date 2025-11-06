package com.example.booking.controller;

import com.example.booking.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    /**
     * Export bookings based on user role
     */
    @GetMapping("/bookings")
    public ResponseEntity<byte[]> exportBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            Authentication authentication) {

        try {
            byte[] excelBytes;
            String username = authentication.getName();

            // Determine role
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isOwner = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

            if (isAdmin) {
                excelBytes = exportService.exportAllBookings(startDate, endDate, status);
            } else if (isOwner) {
                excelBytes = exportService.exportOwnerBookings(username, startDate, endDate);
            } else {
                excelBytes = exportService.exportUserBookings(username, startDate, endDate);
            }

            String filename = generateFilename("訂單明細");
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set("Content-Disposition", "attachment; filename=bookings.xlsx; filename*=UTF-8''" + encoded);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (IOException e) {
            logger.error("匯出 Excel 時發生錯誤", e);
            return ResponseEntity.internalServerError()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("匯出失敗，請稍後再試或聯絡系統管理員".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("發生未預期的錯誤", e);
            return ResponseEntity.internalServerError()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("系統錯誤，請聯絡管理員".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Export admin-specific report
     */
    @GetMapping("/admin/bookings")
    public ResponseEntity<byte[]> exportAdminBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            Authentication authentication) {

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity.status(403).build();
        }

        try {
            byte[] excelBytes = exportService.exportAllBookings(startDate, endDate, status);
            String filename = generateFilename("管理員訂單報表");
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set("Content-Disposition", "attachment; filename=admin-report.xlsx; filename*=UTF-8''" + encoded);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (IOException e) {
            logger.error("匯出 Excel 時發生錯誤", e);
            return ResponseEntity.internalServerError()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("匯出失敗，請稍後再試或聯絡系統管理員".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("發生未預期的錯誤", e);
            return ResponseEntity.internalServerError()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("系統錯誤，請聯絡管理員".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Export owner revenue report
     */
    @GetMapping("/owner/revenue")
    public ResponseEntity<byte[]> exportOwnerRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {

        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (!isOwner) {
            return ResponseEntity.status(403).build();
        }

        try {
            String username = authentication.getName();
            byte[] excelBytes = exportService.exportOwnerBookings(username, startDate, endDate);
            String filename = generateFilename("營收報表");
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.set("Content-Disposition", "attachment; filename=owner-revenue.xlsx; filename*=UTF-8''" + encoded);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (IOException e) {
            logger.error("匯出 Excel 時發生錯誤", e);
            return ResponseEntity.internalServerError()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("匯出失敗，請稍後再試或聯絡系統管理員".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("發生未預期的錯誤", e);
            return ResponseEntity.internalServerError()
                    .header("Content-Type", "text/plain; charset=UTF-8")
                    .body("系統錯誤，請聯絡管理員".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Generate filename with timestamp
     */
    private String generateFilename(String prefix) {
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String time = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"));
        return String.format("%s_%s_%s.xlsx", prefix, timestamp, time);
    }
}

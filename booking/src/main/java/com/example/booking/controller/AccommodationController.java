package com.example.booking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.example.booking.model.Accommodation;
import com.example.booking.service.BookingService;

// Swagger annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/accommodations")
@Tag(name = "Accommodations", description = "住宿資訊查詢 API")
public class AccommodationController {

    private final BookingService bookingService;

    public AccommodationController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Operation(
        summary = "取得所有住宿",
        description = "回傳系統中所有可用的住宿列表，包含住宿名稱、地點、描述、價格等資訊"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得住宿列表"),
        @ApiResponse(responseCode = "500", description = "伺服器錯誤")
    })
    public List<Accommodation> list() {
        return bookingService.getAllAccommodations();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "取得單一住宿",
        description = "根據住宿 ID 查詢詳細資訊"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得住宿資訊"),
        @ApiResponse(responseCode = "404", description = "找不到該住宿")
    })
    public Accommodation getById(
        @Parameter(description = "住宿 ID", required = true, example = "1")
        @PathVariable Long id
    ) {
        return bookingService.getAllAccommodations()
                .stream()
                .filter(acc -> acc.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/search")
    @Operation(
        summary = "搜尋住宿",
        description = "根據地點關鍵字搜尋住宿，支援模糊搜尋且不區分大小寫"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得搜尋結果"),
        @ApiResponse(responseCode = "400", description = "搜尋參數錯誤")
    })
    public List<Accommodation> searchByLocation(
        @Parameter(description = "地點關鍵字", required = true, example = "台北")
        @RequestParam String location
    ) {
        return bookingService.searchByLocation(location);
    }

    @GetMapping("/available")
    @Operation(
        summary = "查詢可用住宿",
        description = "根據入住和退房日期查詢該時段內可預訂的住宿"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "成功取得可用住宿"),
        @ApiResponse(responseCode = "400", description = "日期參數錯誤")
    })
    public List<Accommodation> getAvailable(
        @Parameter(description = "入住日期", required = true, example = "2025-01-15")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
        @Parameter(description = "退房日期", required = true, example = "2025-01-18")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        return bookingService.getAvailableAccommodations(checkIn, checkOut);
    }
}

package com.example.booking.service;

import com.example.booking.model.*;
import com.example.booking.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    @Autowired private AccommodationRepository accommodationRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private RoomTypeRepository roomTypeRepo;

    // === 初始化資料 ===
    @PostConstruct
    public void initData() {
        System.out.println("🔧 初始化資料檢查開始...");

        // 如果 admin 已存在就略過帳號與住宿建立
        if (userRepo.findByUsername("admin").isPresent()) {
            System.out.println("⚙️ 略過帳號與住宿初始化：已存在 admin 帳號");
        } else {
            // 建立帳號
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin123"));
            admin.setRole("ADMIN");

            User user = new User();
            user.setUsername("user");
            user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("user123"));
            user.setRole("USER");

            userRepo.saveAll(List.of(admin, user));
            System.out.println("✅ 已建立帳號：admin / user");

            // 建立住宿
            Accommodation acc1 = new Accommodation();
            acc1.setName("日安旅館");
            acc1.setDescription("溫馨雙人房，交通便利");
            acc1.setLocation("台北市信義區");
            acc1.setPricePerNight(new BigDecimal("1800"));

            Accommodation acc2 = new Accommodation();
            acc2.setName("海景飯店");
            acc2.setDescription("面海房型附早餐");
            acc2.setLocation("花蓮縣壽豐鄉");
            acc2.setPricePerNight(new BigDecimal("3200"));

            accommodationRepo.saveAll(List.of(acc1, acc2));
            System.out.println("✅ 已建立住宿資料");
        }

        // === 若房型資料為空，則初始化房型 ===
        if (roomTypeRepo.count() == 0) {
            List<Accommodation> accList = accommodationRepo.findAll();
            List<RoomType> roomTypes = new ArrayList<>();

            for (Accommodation acc : accList) {
                RoomType rt1 = new RoomType();
                rt1.setAccommodation(acc);
                rt1.setName("標準雙人房");
                rt1.setDescription(acc.getDescription() + "｜標準雙人房");
                rt1.setPricePerNight(acc.getPricePerNight());
                rt1.setTotalRooms(5);
                roomTypes.add(rt1);

                RoomType rt2 = new RoomType();
                rt2.setAccommodation(acc);
                rt2.setName("豪華房");
                rt2.setDescription(acc.getDescription() + "｜豪華加大床");
                rt2.setPricePerNight(acc.getPricePerNight().multiply(new BigDecimal("1.2")));
                rt2.setTotalRooms(3);
                roomTypes.add(rt2);
            }

            roomTypeRepo.saveAll(roomTypes);
            System.out.println("✅ 已建立房型資料：" + roomTypes.size() + " 筆");
        } else {
            System.out.println("⚙️ 房型已存在，略過初始化");
        }

        System.out.println("🎉 初始化程序完成");
    }

    // === 以住宿 ID 下單（相容舊版）===
    public Booking book(long accommodationId, LocalDate checkIn, LocalDate checkOut) {
        List<RoomType> rts = roomTypeRepo.findByAccommodationId(accommodationId);
        if (rts.isEmpty()) {
            throw new RuntimeException("此住宿尚無可訂房型");
        }
        RoomType first = rts.get(0);
        return bookByRoomType(first.getId(), checkIn, checkOut, 1);
    }

    // === 以房型 ID 下單（正式邏輯）===
    public Booking bookByRoomType(long roomTypeId, LocalDate checkIn, LocalDate checkOut, int quantity) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new RuntimeException("日期區間不合法");
        }
        if (quantity <= 0) {
            throw new RuntimeException("預訂數量需大於 0");
        }

        String username = getLoggedInUsername();
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到用戶：" + username));

        RoomType rt = roomTypeRepo.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("找不到房型 ID=" + roomTypeId));

        Long alreadyBooked = bookingRepo.sumBookedQuantityBetween(roomTypeId, checkIn, checkOut);
        int totalRooms = rt.getTotalRooms();
        long willBe = alreadyBooked + quantity;

        if (willBe > totalRooms) {
            throw new RuntimeException("庫存不足，該日期區間剩餘：" + Math.max(totalRooms - alreadyBooked, 0));
        }

        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days <= 0) {
            throw new RuntimeException("入住/退房日期至少需 1 晚");
        }

        BigDecimal totalPrice = rt.getPricePerNight()
                .multiply(BigDecimal.valueOf(days))
                .multiply(BigDecimal.valueOf(quantity));

        Booking booking = new Booking(null, checkIn, checkOut, rt, user, quantity, totalPrice);
        Booking saved = bookingRepo.save(booking);
        System.out.println("✅ 新訂單建立成功：" + saved.getId());
        return saved;
    }

    // === 取得登入使用者 ===
    private String getLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) return ud.getUsername();
        return principal.toString();
    }

    // === 查詢 ===
    public List<Accommodation> getAllAccommodations() {
        return accommodationRepo.findAll();
    }

    public List<Accommodation> searchByLocation(String location) {
        if (location == null || location.isBlank()) {
            return accommodationRepo.findAll();
        }
        return accommodationRepo.findByLocationContainingIgnoreCase(location);
    }

    public List<Booking> getBookingsForUser(String username) {
        return bookingRepo.findByUserUsername(username);
    }

    public List<Booking> getAllBookings() {
        List<Booking> bookings = bookingRepo.findAll();

        // 觸發 Lazy 載入，確保 JSON 有住宿名稱
        for (Booking b : bookings) {
            if (b.getRoomType() != null && b.getRoomType().getAccommodation() != null) {
                b.getRoomType().getAccommodation().getName(); // Hibernate 初始化
            }
        }

        return bookings;
    }


    public List<Accommodation> getAvailableAccommodations(LocalDate checkIn, LocalDate checkOut) {
        return accommodationRepo.findAll();
    }

    // === 一般用戶取消訂單（需為訂單所有者） ===
    public Booking cancelBooking(Long bookingId, String username) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到訂單 ID=" + bookingId));

        // 檢查是否為訂單所有者
        if (!booking.getUser().getUsername().equals(username)) {
            throw new RuntimeException("沒有權限取消此訂單");
        }

        // 檢查訂單狀態
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("訂單已取消");
        }

        // 檢查日期（不能取消已經開始的住宿）
        LocalDate today = LocalDate.now();
        if (!today.isBefore(booking.getCheckIn())) {
            throw new RuntimeException("已開始入住或入住當日，無法取消");
        }

        booking.setStatus("CANCELLED");
        return bookingRepo.save(booking);
    }

    // === 管理員取消訂單（可取消任意訂單） ===
    public Booking cancelBookingByAdmin(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到訂單 ID=" + bookingId));

        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("訂單已取消");
        }

        booking.setStatus("CANCELLED");
        return bookingRepo.save(booking);
    }
}

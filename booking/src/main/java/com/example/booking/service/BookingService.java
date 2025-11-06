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
            admin.setRole("ROLE_ADMIN");

            User user = new User();
            user.setUsername("user");
            user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("user123"));
            user.setRole("ROLE_USER"); // 修正：原本錯誤地設置給了admin

            // 建立房東帳號
            User owner = new User();
            owner.setUsername("owner");
            owner.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("owner123"));
            owner.setRole("ROLE_OWNER");

            userRepo.saveAll(List.of(admin, user, owner));
            System.out.println("✅ 已建立帳號：admin / user / owner");

            // 建立住宿
            Accommodation acc1 = new Accommodation();
            acc1.setName("日安旅館");
            acc1.setDescription("溫馨雙人房，交通便利");
            acc1.setLocation("台北市中山區");
            acc1.setPricePerNight(BigDecimal.valueOf(2000));
            acc1.setOwner(owner); // 設定擁有者

            Accommodation acc2 = new Accommodation();
            acc2.setName("海景villa");
            acc2.setDescription("豪華海景房，適合度假");
            acc2.setLocation("墾丁大街上");
            acc2.setPricePerNight(BigDecimal.valueOf(5000));
            acc2.setOwner(owner); // 設定擁有者

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
        booking.setStatus("PENDING"); // 設置初始狀態為待確認
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
        return bookingRepo.findByUserUsernameFetchAll(username);
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.findAllWithRelations();
    }


    @SuppressWarnings("unused")
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

    // === 房東專用方法 ===

    // 取得房東的住宿清單
    public List<Accommodation> getAccommodationsForOwner(String username) {
        return accommodationRepo.findByOwnerUsername(username);
    }

    // 新增住宿
    public Accommodation createAccommodation(Accommodation newAccommodation, String username) {
        User owner = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到用戶：" + username));

        newAccommodation.setOwner(owner);
        return accommodationRepo.save(newAccommodation);
    }

    // 更新住宿
    public Accommodation updateAccommodation(Long id, Accommodation updatedAccommodation, String username) {
        Accommodation existing = accommodationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到住宿 ID=" + id));

        // 所有權檢查
        if (!existing.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("無權限修改此住宿");
        }

        // 只更新允許的欄位
        existing.setName(updatedAccommodation.getName());
        existing.setLocation(updatedAccommodation.getLocation());
        existing.setDescription(updatedAccommodation.getDescription());
        existing.setPricePerNight(updatedAccommodation.getPricePerNight());
        existing.setAmenities(updatedAccommodation.getAmenities());

        return accommodationRepo.save(existing);
    }

    // 刪除住宿
    public void deleteAccommodation(Long id, String username) {
        Accommodation existing = accommodationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到住宿 ID=" + id));

        // 所有權檢查
        if (!existing.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("無權限刪除此住宿");
        }

        accommodationRepo.deleteById(id);
    }

    // 獲取房型列表
    public List<RoomType> getRoomTypesForAccommodation(Long accId) {
        return roomTypeRepo.findByAccommodationId(accId);
    }

    // 檢查住宿所有權（輔助方法）
    public void checkAccommodationOwnership(Long accId, String username) {
        Accommodation acc = accommodationRepo.findById(accId)
                .orElseThrow(() -> new RuntimeException("找不到住宿 ID=" + accId));

        if (!acc.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("無權限操作此住宿");
        }
    }

    // 新增房型
    public RoomType createRoomType(Long accId, RoomType newRoomType, String username) {
        // 檢查住宿所有權
        checkAccommodationOwnership(accId, username);

        Accommodation acc = accommodationRepo.findById(accId)
                .orElseThrow(() -> new RuntimeException("找不到住宿 ID=" + accId));

        newRoomType.setAccommodation(acc);
        return roomTypeRepo.save(newRoomType);
    }

    // 刪除房型
    public void deleteRoomType(Long roomTypeId, String username) {
        RoomType roomType = roomTypeRepo.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("找不到房型 ID=" + roomTypeId));

        // 所有權檢查
        if (!roomType.getAccommodation().getOwner().getUsername().equals(username)) {
            throw new RuntimeException("無權限刪除此房型");
        }

        roomTypeRepo.deleteById(roomTypeId);
    }

    // === 房東查看自己住宿的訂單 ===
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public List<Booking> getBookingsForOwner(String username) {
        return bookingRepo.findByOwnerUsernameFetchAll(username);
    }

    // === 房東確認訂單 ===
    public Booking confirmBookingByOwner(Long bookingId, String ownerUsername) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到訂單 ID=" + bookingId));

        // 檢查是否為該房東的住宿
        if (!booking.getRoomType().getAccommodation().getOwner().getUsername().equals(ownerUsername)) {
            throw new RuntimeException("無權限確認此訂單");
        }

        // 檢查訂單狀態
        if ("CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("訂單已經確認過了");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("已取消的訂單無法確認");
        }

        booking.setStatus("CONFIRMED");
        return bookingRepo.save(booking);
    }

    // === 房東取消訂單 ===
    public Booking cancelBookingByOwner(Long bookingId, String ownerUsername) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到訂單 ID=" + bookingId));

        // 檢查是否為該房東的住宿
        if (!booking.getRoomType().getAccommodation().getOwner().getUsername().equals(ownerUsername)) {
            throw new RuntimeException("無權限取消此訂單");
        }

        // 檢查訂單狀態
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("訂單已經取消過了");
        }

        booking.setStatus("CANCELLED");
        return bookingRepo.save(booking);
    }

    // === 管理員確認訂單 ===
    public Booking confirmBookingByAdmin(Long bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到訂單 ID=" + bookingId));

        // 檢查訂單狀態
        if ("CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException("訂單已經確認過了");
        }
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("已取消的訂單無法確認");
        }

        booking.setStatus("CONFIRMED");
        return bookingRepo.save(booking);
    }
}

package com.example.booking.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.booking.model.Accommodation;
import com.example.booking.model.Booking;
import com.example.booking.model.User;
import com.example.booking.repository.AccommodationRepository;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private AccommodationRepository accommodationRepo;

    @Autowired
    private BookingRepository bookingRepo;

    @Autowired
    private UserRepository userRepo;

    // =============================
    // 初始化資料（僅第一次執行）
    // =============================
    @PostConstruct
    public void initData() {
        long userCount = userRepo.count();
        long accCount = accommodationRepo.count();

        if (userCount > 0 && accCount > 0) {
            System.out.println("⚙️ 資料已存在，略過初始化");
            return;
        }

        System.out.println("🔊 開始初始化資料...");
        bookingRepo.deleteAll();

        // === 初始化使用者 ===
        if (userCount == 0) {
            List<User> users = List.of(
                    createUser("admin", "password", "ROLE_ADMIN"),
                    createUser("user1", "123456", "ROLE_USER"),
                    createUser("user2", "123456", "ROLE_USER"),
                    createUser("test", "test", "ROLE_USER")
            );
            userRepo.saveAll(users);
            System.out.println("✅ 新增使用者 " + users.size() + " 筆");
        }

        // === 初始化住宿 ===
        if (accCount == 0) {
            List<Accommodation> accs = List.of(
                    createAccommodation("Spring Hotel", "台北", "車站附近", new BigDecimal("2500"), "WiFi, 早餐, 停車場"),
                    createAccommodation("Sea Resort", "高雄", "海景第一排", new BigDecimal("3800"), "WiFi, 游泳池, 健身房"),
                    createAccommodation("山城民宿", "南投", "森林環繞", new BigDecimal("1800"), "WiFi, 停車場"),
                    createAccommodation("海景民宿", "花蓮", "面海第一排", new BigDecimal("2200"), "WiFi, 早餐, 海景陽台"),
                    createAccommodation("城市旅店", "台中", "鄰近車站", new BigDecimal("1600"), "WiFi, 早餐")
            );
            accommodationRepo.saveAll(accs);
            System.out.println("✅ 新增住宿 " + accs.size() + " 筆");
        }

        System.out.println("🎉 初始化完成！");
        System.out.println("📝 測試帳號：");
        System.out.println("   - admin / password");
        System.out.println("   - user1 / 123456");
        System.out.println("   - user2 / 123456");
        System.out.println("   - test / test");
    }

    private User createUser(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return user;
    }

    private Accommodation createAccommodation(String name, String location, String description,
                                              BigDecimal price, String amenities) {
        Accommodation a = new Accommodation();
        a.setName(name);
        a.setLocation(location);
        a.setDescription(description);
        a.setPricePerNight(price);
        a.setAmenities(amenities);
        return a;
    }

    // =============================
    // 業務邏輯
    // =============================

    public List<Accommodation> getAllAccommodations() {
        return accommodationRepo.findAll();
    }

    public List<Booking> getBookingsForUser(String username) {
        return bookingRepo.findByUserUsername(username);
    }

    public Booking book(long accommodationId, LocalDate checkIn, LocalDate checkOut) {
        String username = getLoggedInUsername();
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("找不到用戶：" + username));
        Accommodation acc = accommodationRepo.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("找不到住宿 ID=" + accommodationId));

        // === 日期衝突檢查 ===
        List<Booking> conflicts = bookingRepo.findConflictingBookings(accommodationId, checkIn, checkOut);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("此住宿在指定日期已被預訂");
        }

        Booking booking = new Booking(null, checkIn, checkOut, acc, user);
        return bookingRepo.save(booking);
    }

    private String getLoggedInUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    public List<Accommodation> searchByLocation(String location) {
        return accommodationRepo.findByLocationContainingIgnoreCase(location);
    }

    public List<Accommodation> getAvailableAccommodations(LocalDate checkIn, LocalDate checkOut) {
        return accommodationRepo.findAvailableAccommodations(checkIn, checkOut);
    }
}

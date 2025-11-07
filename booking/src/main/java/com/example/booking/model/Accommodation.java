package com.example.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "accommodations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 避免 Lazy 加載報錯
@Schema(description = "住宿資訊")
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "住宿 ID", example = "1")
    private Long id;

    @Schema(description = "住宿名稱", example = "日安旅館", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "地點", example = "台北市中山區", requiredMode = Schema.RequiredMode.REQUIRED)
    private String location;

    @Schema(description = "描述", example = "溫馨雙人房，交通便利")
    private String description;

    @Column(name = "price_per_night", nullable = false, precision = 10, scale = 2)
    @Schema(description = "每晚價格", example = "2000.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal pricePerNight = BigDecimal.valueOf(1000); // 預設1000元

    @Column(length = 500)
    @Schema(description = "設施", example = "WiFi, 停車場, 早餐")
    private String amenities; // 例："WiFi, 停車場, 早餐"

    // === 新增：與 RoomType 的一對多關聯 ===
    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"accommodation", "bookings"}) // 防止雙向遞迴
    private List<RoomType> roomTypes;

    // === 新增：與 User 的多對一關聯 ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "roles"})
    private User owner;

    // === 建構子 ===
    public Accommodation() {}

    public Accommodation(Long id, String name, String location, String description, BigDecimal pricePerNight) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.pricePerNight = pricePerNight;
    }

    // === Getter / Setter ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public List<RoomType> getRoomTypes() { return roomTypes; }
    public void setRoomTypes(List<RoomType> roomTypes) { this.roomTypes = roomTypes; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}

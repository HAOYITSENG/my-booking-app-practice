package com.example.booking.repository;

import com.example.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserUsername(String username);

    // 查重疊（同房型且日期有交集，且非取消狀態）
    @Query("""
           SELECT b FROM Booking b
           WHERE b.roomType.id = :roomTypeId
             AND b.checkIn < :checkOut
             AND b.checkOut > :checkIn
             AND b.status != 'CANCELLED'
           """)
    List<Booking> findConflictingBookings(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);

    // 匯總重疊區間的已預訂間數（排除已取消訂單）
    @Query("""
           SELECT COALESCE(SUM(b.bookedQuantity), 0)
           FROM Booking b
           WHERE b.roomType.id = :roomTypeId
             AND b.checkIn < :checkOut
             AND b.checkOut > :checkIn
             AND b.status != 'CANCELLED'
           """)
    Long sumBookedQuantityBetween(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
}

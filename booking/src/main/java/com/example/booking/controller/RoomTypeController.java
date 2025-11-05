package com.example.booking.controller;

import com.example.booking.model.RoomType;
import com.example.booking.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    @Autowired
    private RoomTypeRepository roomTypeRepo;

    // 查所有房型（管理用）
    @GetMapping
    public List<RoomType> getAllRoomTypes() {
        return roomTypeRepo.findAll();
    }

    // 查某住宿底下的房型清單
    @GetMapping("/by-accommodation/{accId}")
    public List<RoomType> getByAccommodation(@PathVariable Long accId) {
        return roomTypeRepo.findByAccommodationId(accId);
    }
}

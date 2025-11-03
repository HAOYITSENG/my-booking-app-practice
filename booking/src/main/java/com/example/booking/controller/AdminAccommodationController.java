package com.example.booking.controller;


import com.example.booking.model.Accommodation;
import com.example.booking.repository.AccommodationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accommodations")
public class AdminAccommodationController {

    private final AccommodationRepository accommodationRepository;

    public AdminAccommodationController(AccommodationRepository accommodationRepository) {
        this.accommodationRepository = accommodationRepository;
    }

    @PostMapping
    public ResponseEntity<?> createAccommodation(@RequestBody Accommodation accommodation) {
        Accommodation saved = accommodationRepository.save(accommodation);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccommodation(@PathVariable Long id,
                                                 @RequestBody Accommodation accommodation) {
        return accommodationRepository.findById(id)
                .map(existing -> {
                    existing.setName(accommodation.getName());
                    existing.setLocation(accommodation.getLocation());
                    existing.setDescription(accommodation.getDescription());
                    existing.setPricePerNight(accommodation.getPricePerNight());
                    return ResponseEntity.ok(accommodationRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccommodation(@PathVariable Long id) {
        if (!accommodationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        accommodationRepository.deleteById(id);
        return ResponseEntity.ok("刪除成功");
    }
}

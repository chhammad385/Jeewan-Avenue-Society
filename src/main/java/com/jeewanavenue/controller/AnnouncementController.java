package com.jeewanavenue.controller;

import com.jeewanavenue.dto.AnnouncementDto;
import com.jeewanavenue.entity.Announcement;
import com.jeewanavenue.service.AnnouncementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    // Return DTO list
    @GetMapping
    public List<AnnouncementDto> getAll() {
        return announcementService.findAll()
                .stream()
                .map(AnnouncementDto::from)
                .collect(Collectors.toList());
    }

    // Active now
    @GetMapping("/active")
    public List<AnnouncementDto> getActive() {
        return announcementService.findActiveNow()
                .stream()
                .map(AnnouncementDto::from)
                .collect(Collectors.toList());
    }

    // Upcoming
    @GetMapping("/upcoming")
    public List<AnnouncementDto> getUpcoming() {
        return announcementService.findUpcoming()
                .stream()
                .map(AnnouncementDto::from)
                .collect(Collectors.toList());
    }

    // Get one
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementDto> getOne(@PathVariable Long id) {
        return announcementService.findById(id)
                .map(AnnouncementDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create (accepts entity fields)
    @PostMapping
    public ResponseEntity<AnnouncementDto> create(@RequestBody Announcement announcement) {
        Announcement saved = announcementService.save(announcement);
        return new ResponseEntity<>(AnnouncementDto.from(saved), HttpStatus.CREATED);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementDto> update(@PathVariable Long id,
                                                  @RequestBody Announcement announcement) {
        try {
            Announcement updated = announcementService.update(id, announcement);
            return ResponseEntity.ok(AnnouncementDto.from(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        announcementService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Health / diagnostics
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Announcements API OK");
    }
}
package com.jeewanavenue.controller;

import com.jeewanavenue.entity.PrayerTimings;
import com.jeewanavenue.service.PrayerTimingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prayer-timings")
public class PrayerTimingsController {

    @Autowired
    private PrayerTimingsService prayerTimingsService;

    // This is a public endpoint for anyone to view prayer times
    @GetMapping
    public ResponseEntity<PrayerTimings> getTimings() {
        return ResponseEntity.ok(prayerTimingsService.getPrayerTimings());
    }

    // This is a protected endpoint for the president to update times
    @PutMapping
    public ResponseEntity<PrayerTimings> updateTimings(@RequestBody PrayerTimings timings) {
        return ResponseEntity.ok(prayerTimingsService.updatePrayerTimings(timings));
    }
}
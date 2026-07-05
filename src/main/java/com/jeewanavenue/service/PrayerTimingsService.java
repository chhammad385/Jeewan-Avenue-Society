package com.jeewanavenue.service;

import com.jeewanavenue.entity.PrayerTimings;
import com.jeewanavenue.repository.PrayerTimingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrayerTimingsService {

    @Autowired
    private PrayerTimingsRepository prayerTimingsRepository;

    public PrayerTimings getPrayerTimings() {
        // Try to find existing row with ID 1, or create and save a default one
        return prayerTimingsRepository.findById(1).orElseGet(() -> {
            PrayerTimings defaultTimings = new PrayerTimings();
            defaultTimings.setId(1);
            return prayerTimingsRepository.save(defaultTimings);
        });
    }

    public PrayerTimings updatePrayerTimings(PrayerTimings timings) {
        // Ensure we're always updating the single row with ID 1
        timings.setId(1);
        return prayerTimingsRepository.save(timings);
    }
}
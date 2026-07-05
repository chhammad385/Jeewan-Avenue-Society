package com.jeewanavenue.repository;

import com.jeewanavenue.entity.PrayerTimings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrayerTimingsRepository extends JpaRepository<PrayerTimings, Integer> {
}
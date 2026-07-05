package com.jeewanavenue.repository;

import com.jeewanavenue.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // Active now = startDatetime <= now AND endDatetime >= now
    List<Announcement> findByStartDatetimeLessThanEqualAndEndDatetimeGreaterThanEqual(
            LocalDateTime start, LocalDateTime end
    );

    // Optional: If you want future announcements
    List<Announcement> findByStartDatetimeGreaterThanOrderByStartDatetimeAsc(LocalDateTime after);
}
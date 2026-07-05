package com.jeewanavenue.service;

import com.jeewanavenue.entity.Announcement;
import com.jeewanavenue.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<Announcement> findAll() {
        return announcementRepository.findAll();
    }

    public Optional<Announcement> findById(Long id) {
        return announcementRepository.findById(id);
    }

    public Announcement save(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public void deleteById(Long id) {
        announcementRepository.deleteById(id);
    }

    public List<Announcement> findActiveNow() {
        LocalDateTime now = LocalDateTime.now();
        return announcementRepository
                .findByStartDatetimeLessThanEqualAndEndDatetimeGreaterThanEqual(now, now);
    }

    public List<Announcement> findUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return announcementRepository.findByStartDatetimeGreaterThanOrderByStartDatetimeAsc(now);
    }

    public Announcement update(Long id, Announcement updated) {
        return announcementRepository.findById(id)
                .map(existing -> {
                    existing.setTitle(updated.getTitle());
                    existing.setContent(updated.getContent());
                    existing.setStartDatetime(updated.getStartDatetime());
                    existing.setEndDatetime(updated.getEndDatetime());
                    return announcementRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Announcement not found id=" + id));
    }
}
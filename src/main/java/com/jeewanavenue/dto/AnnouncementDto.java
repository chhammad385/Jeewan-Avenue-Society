package com.jeewanavenue.dto;

import com.jeewanavenue.entity.Announcement;

import java.time.LocalDateTime;

public record AnnouncementDto(
        Long id,
        String title,
        String content,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime
) {
    public static AnnouncementDto from(Announcement a) {
        return new AnnouncementDto(
                a.getId(),
                a.getTitle(),
                a.getContent(),
                a.getStartDatetime(),
                a.getEndDatetime()
        );
    }
}
package com.jeewanavenue.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "prayer_timings")
public class PrayerTimings {
    @Id
    private Integer id = 1; // Default to 1

    @Column(nullable = true)
    private String fajr = "05:00"; // Default time
    
    @Column(nullable = true)
    private String dhuhr = "12:30"; // Default time
    
    @Column(nullable = true)
    private String asr = "16:00"; // Default time
    
    @Column(nullable = true)
    private String maghrib = "18:00"; // Default time
    
    @Column(nullable = true)
    private String isha = "19:30"; // Default time
    
    @Column(nullable = true)
    private String jumma = "13:00"; // Default time

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFajr() { return fajr; }
    public void setFajr(String fajr) { this.fajr = fajr; }
    public String getDhuhr() { return dhuhr; }
    public void setDhuhr(String dhuhr) { this.dhuhr = dhuhr; }
    public String getAsr() { return asr; }
    public void setAsr(String asr) { this.asr = asr; }
    public String getMaghrib() { return maghrib; }
    public void setMaghrib(String maghrib) { this.maghrib = maghrib; }
    public String getIsha() { return isha; }
    public void setIsha(String isha) { this.isha = isha; }
    public String getJumma() { return jumma; }
    public void setJumma(String jumma) { this.jumma = jumma; }
}
package com.jeewanavenue.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "plots")
public class Plot {
    @Id
    private String id; // e.g., H-001

    private String type;
    private String status;
    private BigDecimal marla;
    private String ownerName;
    private String ownerPhone;
    private String renterName;
    private String renterPhone;
    
    @Column(name = "resident_owner")
    private Boolean residentOwner = false;
    
    @Column(name = "resident_renter")
    private Boolean residentRenter = false;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getMarla() {
        return marla;
    }

    public void setMarla(BigDecimal marla) {
        this.marla = marla;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    public String getRenterPhone() {
        return renterPhone;
    }

    public void setRenterPhone(String renterPhone) {
        this.renterPhone = renterPhone;
    }

    public Boolean getResidentOwner() {
        return residentOwner;
    }

    public void setResidentOwner(Boolean residentOwner) {
        this.residentOwner = residentOwner;
    }

    public Boolean getResidentRenter() {
        return residentRenter;
    }

    public void setResidentRenter(Boolean residentRenter) {
        this.residentRenter = residentRenter;
    }
}
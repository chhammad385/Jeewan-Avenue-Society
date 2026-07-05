package com.jeewanavenue.dto;

public class PlotDTO {
    private String id; // plot_no
    private String type; // property_type (house/shop)
    private String status; // built_status (completed/under-construction/vacant/residential)
    private String ownerName;
    private String ownerPhone;
    private String renterName;
    private String renterPhone;
    private boolean residentOwner; // true if status is "Owned" or "Both"
    private boolean residentRenter; // true if status is "Rented" or "Both"
    private Long userId; // reference to user

    // Constructors
    public PlotDTO() {}

    public PlotDTO(String id, String type, String status, String ownerName, String ownerPhone,
                   String renterName, String renterPhone, boolean residentOwner, boolean residentRenter, Long userId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.renterName = renterName;
        this.renterPhone = renterPhone;
        this.residentOwner = residentOwner;
        this.residentRenter = residentRenter;
        this.userId = userId;
    }

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

    public boolean isResidentOwner() {
        return residentOwner;
    }

    public void setResidentOwner(boolean residentOwner) {
        this.residentOwner = residentOwner;
    }

    public boolean isResidentRenter() {
        return residentRenter;
    }

    public void setResidentRenter(boolean residentRenter) {
        this.residentRenter = residentRenter;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

package com.jeewanavenue.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "plot_no")
    private String plotNo;

    @Column(name = "plot_size_marla")
    private String plotSizeMarla;

    @Column(name = "property_type")
    private String propertyType;

    @Column(name = "no_of_shops")
    private Integer noOfShops;

    private String status;

    @Column(name = "renter_name")
    private String renterName;

    @Column(name = "renter_phone_no")
    private String renterPhoneNo;

    @Column(name = "renter_cnic")
    private String renterCnic;

    @Column(name = "renter_previous_address", length = 500)
    private String renterPreviousAddress;

    @Column(name = "previous_address")
    private String previousAddress;

    @Column(unique = true)
    private String cnic;

    @Column(name = "family_members")
    private Integer familyMembers;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(name = "profile_picture_path")
    private String profilePicturePath;

    @Column(name = "last_feedback_date")
    private LocalDate lastFeedbackDate;

    @Column(name = "last_access_was_no_staff", nullable = false, columnDefinition = "boolean default false")
    private Boolean lastAccessWasNoStaff = false;

    @Column(name = "account_status", nullable = false, columnDefinition = "varchar(20) default 'Active'")
    private String accountStatus = "Active";

    @Column(name = "account_balance", nullable = false, columnDefinition = "decimal(10,2) default 0.00")
    private BigDecimal accountBalance = BigDecimal.ZERO;

    @Column(name = "built_status")
    private String builtStatus;

    @Column(name = "blood_group")
    private String bloodGroup;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getPlotNo() {
        return plotNo;
    }

    public void setPlotNo(String plotNo) {
        this.plotNo = plotNo;
    }

    public String getPlotSizeMarla() {
        return plotSizeMarla;
    }

    public void setPlotSizeMarla(String plotSizeMarla) {
        this.plotSizeMarla = plotSizeMarla;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPreviousAddress() {
        return previousAddress;
    }

    public void setPreviousAddress(String previousAddress) {
        this.previousAddress = previousAddress;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public Integer getFamilyMembers() {
        return familyMembers;
    }

    public void setFamilyMembers(Integer familyMembers) {
        this.familyMembers = familyMembers;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfilePicturePath() {
        return profilePicturePath;
    }

    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }

    public LocalDate getLastFeedbackDate() {
        return lastFeedbackDate;
    }

    public void setLastFeedbackDate(LocalDate lastFeedbackDate) {
        this.lastFeedbackDate = lastFeedbackDate;
    }

    public Boolean getLastAccessWasNoStaff() {
        return lastAccessWasNoStaff;
    }

    public void setLastAccessWasNoStaff(Boolean lastAccessWasNoStaff) {
        this.lastAccessWasNoStaff = lastAccessWasNoStaff;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getBuiltStatus() {
        return builtStatus;
    }

    public void setBuiltStatus(String builtStatus) {
        this.builtStatus = builtStatus;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public Integer getNoOfShops() {
        return noOfShops;
    }

    public void setNoOfShops(Integer noOfShops) {
        this.noOfShops = noOfShops;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    public String getRenterPhoneNo() {
        return renterPhoneNo;
    }

    public void setRenterPhoneNo(String renterPhoneNo) {
        this.renterPhoneNo = renterPhoneNo;
    }

    public String getRenterCnic() {
        return renterCnic;
    }

    public void setRenterCnic(String renterCnic) {
        this.renterCnic = renterCnic;
    }

    public String getRenterPreviousAddress() {
        return renterPreviousAddress;
    }

    public void setRenterPreviousAddress(String renterPreviousAddress) {
        this.renterPreviousAddress = renterPreviousAddress;
    }

    // Utility methods for account balance management
    public void addToBalance(BigDecimal amount) {
        if (amount != null) {
            this.accountBalance = this.accountBalance.add(amount);
        }
    }

    public void subtractFromBalance(BigDecimal amount) {
        if (amount != null) {
            this.accountBalance = this.accountBalance.subtract(amount);
        }
    }

    public boolean hasDue() {
        return this.accountBalance.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasDebit() {
        return this.accountBalance.compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getDueAmount() {
        return this.accountBalance.compareTo(BigDecimal.ZERO) > 0 ? this.accountBalance : BigDecimal.ZERO;
    }

    public BigDecimal getDebitAmount() {
        return this.accountBalance.compareTo(BigDecimal.ZERO) < 0 ? this.accountBalance.abs() : BigDecimal.ZERO;
    }
}
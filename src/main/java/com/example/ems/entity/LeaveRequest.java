package com.example.ems.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "leave_application")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;
    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;
    @Column(nullable = false)
    private Integer days;
    @Column(name = "leave_type", nullable = false, length = 50)
    private String leaveType;
    @Column(length = 500)
    private String reason;
    @Column(nullable = false)
    private String status = "PENDING";
    @Column(name = "applied_on", nullable = false)
    private LocalDate appliedOn;
    private String approver;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LeaveRequest() {
    }

    @PrePersist
    void create() {
        LocalDateTime n = LocalDateTime.now();
        if (appliedOn == null) {
            appliedOn = LocalDate.now();
        
        }if (status == null) {
            status = "PENDING";
        
        }if (createdAt == null) {
            createdAt = n;
        
        }if (updatedAt == null) {
            updatedAt = n;
    
        }}

    @PreUpdate
    void update() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long v) {
        employeeId = v;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate v) {
        fromDate = v;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate v) {
        toDate = v;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer v) {
        days = v;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String v) {
        leaveType = v;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String v) {
        reason = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public LocalDate getAppliedOn() {
        return appliedOn;
    }

    public void setAppliedOn(LocalDate v) {
        appliedOn = v;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String v) {
        approver = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        createdAt = v;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime v) {
        updatedAt = v;
    }
}

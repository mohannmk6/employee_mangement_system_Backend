package com.example.ems.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee_leave_balance")
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "employee_id", nullable = false, unique = true)
    private Long employeeId;
    @Column(name = "casual_total", nullable = false)
    private Integer casualTotal = 12;
    @Column(name = "casual_remaining", nullable = false)
    private Integer casualRemaining = 12;
    @Column(name = "sick_total", nullable = false)
    private Integer sickTotal = 12;
    @Column(name = "sick_remaining", nullable = false)
    private Integer sickRemaining = 12;
    @Column(name = "earned_total", nullable = false)
    private Integer earnedTotal = 12;
    @Column(name = "earned_remaining", nullable = false)
    private Integer earnedRemaining = 12;
    @Column(name = "unpaid_total", nullable = false)
    private Integer unpaidTotal = 12;
    @Column(name = "unpaid_remaining", nullable = false)
    private Integer unpaidRemaining = 12;

    public LeaveBalance() {
    }

    public static LeaveBalance forEmployee(Long id) {
        LeaveBalance b = new LeaveBalance();
        b.employeeId = id;
        return b;
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

    public Integer getCasualTotal() {
        return casualTotal;
    }

    public void setCasualTotal(Integer v) {
        casualTotal = v;
    }

    public Integer getCasualRemaining() {
        return casualRemaining;
    }

    public void setCasualRemaining(Integer v) {
        casualRemaining = v;
    }

    public Integer getSickTotal() {
        return sickTotal;
    }

    public void setSickTotal(Integer v) {
        sickTotal = v;
    }

    public Integer getSickRemaining() {
        return sickRemaining;
    }

    public void setSickRemaining(Integer v) {
        sickRemaining = v;
    }

    public Integer getEarnedTotal() {
        return earnedTotal;
    }

    public void setEarnedTotal(Integer v) {
        earnedTotal = v;
    }

    public Integer getEarnedRemaining() {
        return earnedRemaining;
    }

    public void setEarnedRemaining(Integer v) {
        earnedRemaining = v;
    }

    public Integer getUnpaidTotal() {
        return unpaidTotal;
    }

    public void setUnpaidTotal(Integer v) {
        unpaidTotal = v;
    }

    public Integer getUnpaidRemaining() {
        return unpaidRemaining;
    }

    public void setUnpaidRemaining(Integer v) {
        unpaidRemaining = v;
    }
}

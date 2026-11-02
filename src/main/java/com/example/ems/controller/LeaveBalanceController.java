package com.example.ems.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ems.entity.LeaveBalance;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.LeaveBalanceRepository;

@RestController
@RequestMapping("/api/leave-balance")
public class LeaveBalanceController {

    private final LeaveBalanceRepository balances;
    private final EmployeeRepository employees;

    public LeaveBalanceController(LeaveBalanceRepository b, EmployeeRepository e) {
        balances = b;
        employees = e;
    }

    @GetMapping("/{employeeId}")
    public LeaveBalance get(@PathVariable Long employeeId) {
        if (!employees.existsById(employeeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
        
        }return balances.findByEmployeeId(employeeId).orElseGet(() -> balances.save(LeaveBalance.forEmployee(employeeId)));
    }
}

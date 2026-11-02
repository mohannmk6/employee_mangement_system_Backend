package com.example.ems.controller;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ems.entity.Employee;
import com.example.ems.entity.LeaveBalance;
import com.example.ems.entity.LeaveRequest;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.LeaveBalanceRepository;
import com.example.ems.repository.LeaveRequestRepository;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveRequestRepository leaves;
    private final EmployeeRepository employees;
    private final LeaveBalanceRepository balances;

    public LeaveController(LeaveRequestRepository l, EmployeeRepository e, LeaveBalanceRepository b) {
        leaves = l;
        employees = e;
        balances = b;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return leaves.findAll().stream().map(this::view).toList();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> apply(@RequestBody LeaveRequest l) {
        if (l.getEmployeeId() == null || !employees.existsById(l.getEmployeeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid employeeId is required");
        
        }if (l.getFromDate() == null || l.getToDate() == null || l.getToDate().isBefore(l.getFromDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid leave dates");
        
        }if (l.getDays() == null || l.getDays() < 1) {
            l.setDays((int) (java.time.temporal.ChronoUnit.DAYS.between(l.getFromDate(), l.getToDate()) + 1));
        
        }l.setLeaveType(l.getLeaveType().toUpperCase());
        l.setStatus("PENDING");
        return ResponseEntity.status(201).body(view(leaves.save(l)));
    }

    @PutMapping("/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id, @RequestParam String approver) {
        LeaveRequest l = find(id);
        if (!"APPROVED".equalsIgnoreCase(l.getStatus())) {
            adjustBalance(l, -1);
        }
        l.setStatus("APPROVED");
        l.setApprover(approver);
        return view(leaves.save(l));
    }

    @PutMapping("/{id}/reject")
    public Map<String, Object> reject(@PathVariable Long id, @RequestParam String approver) {
        LeaveRequest l = find(id);
        if ("APPROVED".equalsIgnoreCase(l.getStatus())) {
            adjustBalance(l, 1);
        
        }l.setStatus("REJECTED");
        l.setApprover(approver);
        return view(leaves.save(l));
    }

    @GetMapping("/upcoming")
    public List<Map<String, Object>> upcoming(@RequestParam Long employeeId) {
        return leaves.findByEmployeeId(employeeId).stream().filter(l -> l.getToDate() != null && !l.getToDate().isBefore(LocalDate.now())).map(this::view).toList();
    }

    private LeaveRequest find(Long id) {
        return leaves.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found"));
    }

    private void adjustBalance(LeaveRequest l, int direction) {
        LeaveBalance b = balances.findByEmployeeId(l.getEmployeeId()).orElseGet(() -> balances.save(LeaveBalance.forEmployee(l.getEmployeeId())));
        int n = l.getDays() == null ? 1 : l.getDays();
        switch (l.getLeaveType().toUpperCase()) {
            case "CASUAL" ->
                b.setCasualRemaining(Math.max(0, Math.min(b.getCasualTotal(), b.getCasualRemaining() + direction * n)));
            case "SICK" ->
                b.setSickRemaining(Math.max(0, Math.min(b.getSickTotal(), b.getSickRemaining() + direction * n)));
            case "EARNED" ->
                b.setEarnedRemaining(Math.max(0, Math.min(b.getEarnedTotal(), b.getEarnedRemaining() + direction * n)));
            case "UNPAID" ->
                b.setUnpaidRemaining(Math.max(0, Math.min(b.getUnpaidTotal(), b.getUnpaidRemaining() + direction * n)));
        }
        balances.save(b);
    }

    private Map<String, Object> view(LeaveRequest l) {
        Map<String, Object> leave = new LinkedHashMap<>();
        leave.put("id", l.getId());
        leave.put("employeeId", l.getEmployeeId());
        leave.put("employee", employees.findById(l.getEmployeeId()).map(Employee::getName).orElse("Unknown"));
        leave.put("leaveType", l.getLeaveType());
        leave.put("fromDate", l.getFromDate());
        leave.put("toDate", l.getToDate());
        leave.put("days", l.getDays());
        leave.put("reason", l.getReason());
        leave.put("appliedOn", l.getAppliedOn());
        leave.put("status", l.getStatus());
        leave.put("approver", l.getApprover());
        leave.put("createdAt", l.getCreatedAt());
        leave.put("updatedAt", l.getUpdatedAt());
        return leave;
    }
}

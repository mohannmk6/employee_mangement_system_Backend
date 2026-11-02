package com.example.ems.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ems.entity.Attendance;
import com.example.ems.repository.AttendanceRepository;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.HolidayRepository;
import com.example.ems.repository.LeaveRequestRepository;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceRepository attendance;
    private final EmployeeRepository employees;
    private final HolidayRepository holidays;
    private final LeaveRequestRepository leaves;

    public AttendanceController(AttendanceRepository a, EmployeeRepository e, HolidayRepository h, LeaveRequestRepository l) {
        attendance = a;
        employees = e;
        holidays = h;
        leaves = l;
    }

    @PostMapping("/check-in/{employeeId}")
    public Attendance checkIn(@PathVariable Long employeeId) {
        valid(employeeId);
        LocalDate today = LocalDate.now();
        Attendance a = attendance.findByEmployeeIdAndDate(employeeId, today).orElseGet(() -> {
            Attendance n = new Attendance();
            n.setEmployeeId(employeeId);
            n.setDate(today);
            return n;
        });
        if (a.getCheckIn() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already checked in today");
        
        }a.setCheckIn(LocalDateTime.now().withNano(0));
        a.setStatus("PRESENT");
        return attendance.save(a);
    }

    @PostMapping("/check-out/{employeeId}")
    public Attendance checkOut(@PathVariable Long employeeId) {
        Attendance atten = attendance.findByEmployeeIdAndDate(employeeId, LocalDate.now()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please check in first"));
        if (atten.getCheckIn() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please check in first");
        
        }if (atten.getCheckOut() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already checked out today");
        
        }atten.setCheckOut(LocalDateTime.now().withNano(0));
        return attendance.save(atten);
    }

    @GetMapping("/today/{employeeId}")
    public Attendance today(@PathVariable Long employeeId) {
        valid(employeeId);
        return attendance.findByEmployeeIdAndDate(employeeId, LocalDate.now()).orElseGet(() -> {
            Attendance a = new Attendance();
            a.setEmployeeId(employeeId);
            a.setDate(LocalDate.now());
            a.setStatus("ABSENT");
            return a;
        });
    }

    @GetMapping("/employee/{employeeId}")
    public List<Attendance> history(@PathVariable Long employeeId) {
        valid(employeeId);
        return attendance.findByEmployeeIdOrderByDateDesc(employeeId);
    }

    @GetMapping("/summary/{employeeId}")
    public Map<String, Object> summary(@PathVariable Long employeeId) {
        valid(employeeId);
        LocalDate today = LocalDate.now(), start = today.withDayOfMonth(1);
        List<Attendance> rows = attendance.findByEmployeeIdAndDateBetween(employeeId, start, today);
        long present = rows.stream().filter(a -> "PRESENT".equalsIgnoreCase(a.getStatus()) && a.getCheckIn() != null).count();
        long absent = rows.stream().filter(a -> "ABSENT".equalsIgnoreCase(a.getStatus())).count();
        long half = rows.stream().filter(a -> "HALF_DAY".equalsIgnoreCase(a.getStatus())).count();
        Attendance t = attendance.findByEmployeeIdAndDate(employeeId, today).orElse(null);
        long holiday = holidays.findAll().stream().filter(h -> h.getHolidayDate() != null && !h.getHolidayDate().isBefore(start) && !h.getHolidayDate().isAfter(today)).count();
        long approved = leaves.findByEmployeeId(employeeId).stream().filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()) && l.getFromDate() != null && l.getFromDate().getMonth() == today.getMonth() && l.getFromDate().getYear() == today.getYear()).mapToLong(l -> l.getDays() == null ? 0 : l.getDays()).sum();
        long working = Math.max(0, ChronoUnit.DAYS.between(start, today) + 1 - holiday);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("employeeId", employeeId);
        m.put("holidayDays", holiday);
        m.put("presentDays", present);
        m.put("absentDays", absent);
        m.put("halfDay", half);
        m.put("totalAttendanceDays", rows.size());
        m.put("approvedLeaveDays", approved);
        m.put("totalWorkingDays", working);
        m.put("attendancePercentage", working == 0 ? 0 : Math.round((present * 100.0 / working) * 100.0) / 100.0);
        m.put("todayStatus", t == null ? null : t.getStatus());
        m.put("checkIn", t == null ? null : t.getCheckIn());
        m.put("checkOut", t == null ? null : t.getCheckOut());
        m.put("present", present);
        m.put("absent", absent);
        return m;
    }

    private void valid(Long id) {
        if (!employees.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found");
    
        }}
}

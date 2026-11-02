package com.example.ems.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ems.entity.Employee;
import com.example.ems.entity.LeaveBalance;
import com.example.ems.entity.UserAccount;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.LeaveBalanceRepository;
import com.example.ems.repository.UserAccountRepository;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employees;
    private final UserAccountRepository users;
    private final LeaveBalanceRepository balances;
    private final PasswordEncoder encoder;

    public EmployeeController(EmployeeRepository e, UserAccountRepository u, LeaveBalanceRepository b, PasswordEncoder p) {
        employees = e;
        users = u;
        balances = b;
        encoder = p;
    }

    @GetMapping
    public List<Employee> all() {
        return employees.findAll();
    }

    @GetMapping("/{id}")
    public Employee one(@PathVariable Long id) {
        return employees.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    @PostMapping
    public ResponseEntity<Employee> create(@RequestBody Employee input) {
        if (input.getEmail() == null || input.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        
        }if (employees.existsByEmailIgnoreCase(input.getEmail()) || users.existsByUsernameIgnoreCase(input.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email or username already exists");
        
        }Employee e = new Employee();
        e.setName(input.getName());
        e.setEmail(input.getEmail().trim());
        e.setRole(input.getRole() == null ? "Employee" : input.getRole());
        e.setDepartment(input.getDepartment());
        e.setPhone(input.getPhone());
        e.setLocation(input.getLocation());
        e.setWebsite(input.getWebsite());
        e.setBio(input.getBio());
        String initial = input.getPassword();
        if (initial == null || initial.isBlank()) {
            initial = "Welcome@123";
        
        }String hash = encoder.encode(initial);
        e.setPassword(hash);
        Employee saved = employees.save(e);
        UserAccount u = new UserAccount();
        u.setUsername(saved.getEmail());
        u.setPassword(hash);
        u.setRole("ROLE_EMPLOYEE");
        u.setEmployeeId(saved.getId());
        users.save(u);
        balances.save(LeaveBalance.forEmployee(saved.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @RequestBody Employee input) {
        Employee e = one(id);
        if (input.getEmail() != null && !input.getEmail().equalsIgnoreCase(e.getEmail()) && employees.existsByEmailIgnoreCase(input.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        
        }e.setName(input.getName());
        e.setEmail(input.getEmail());
        e.setRole(input.getRole());
        e.setDepartment(input.getDepartment());
        e.setPhone(input.getPhone());
        e.setLocation(input.getLocation());
        e.setWebsite(input.getWebsite());
        e.setBio(input.getBio());
        if (input.getPassword() != null && !input.getPassword().isBlank()) {
            String hash = encoder.encode(input.getPassword());
            e.setPassword(hash);
            users.findByEmployeeId(id).ifPresent(u -> {
                u.setPassword(hash);
                users.save(u);
            });
        }
        return employees.save(e);
    }

  @DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {

    Employee employee = employees.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Employee not found"));

    employee.setActive(false);
    employees.save(employee);

    return ResponseEntity.noContent().build();
}
}

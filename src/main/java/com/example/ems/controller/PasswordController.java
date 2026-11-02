package com.example.ems.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ems.entity.UserAccount;
import com.example.ems.repository.UserAccountRepository;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;

    public PasswordController(UserAccountRepository u, PasswordEncoder p) {
        users = u;
        encoder = p;
    }

    @PutMapping("/change")
    public Map<String, String> change(@RequestParam String username, @RequestBody Map<String, String> body) {
        UserAccount u = users.findByUsernameIgnoreCase(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!encoder.matches(body.getOrDefault("currentPassword", ""), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        
        }String next = body.getOrDefault("newPassword", "");
        if (next.length() < 8 || next.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be 8 to 72 characters");
        
        }u.setPassword(encoder.encode(next));
        users.save(u);
        return Map.of("message", "Password changed successfully");
    }
}

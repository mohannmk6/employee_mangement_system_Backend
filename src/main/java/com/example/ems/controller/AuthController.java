package com.example.ems.controller;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ems.entity.Employee;
import com.example.ems.entity.UserAccount;
import com.example.ems.repository.EmployeeRepository;
import com.example.ems.repository.UserAccountRepository;
import com.example.ems.security.JwtService;

import jakarta.mail.internet.MimeMessage;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository users;
    private final EmployeeRepository employees;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final JavaMailSender mail;
    @Value("${spring.mail.username:}")
    private String sender;
    private final Map<String, OtpEntry> otps = new ConcurrentHashMap<>();
    private final Map<String, ResetEntry> resetTokens = new ConcurrentHashMap<>();

    private record OtpEntry(String code, Instant expires) {}

    private record ResetEntry(String username, Instant expires) {}

    public AuthController(UserAccountRepository u, EmployeeRepository e, PasswordEncoder p, JwtService j, JavaMailSender m) {
        users = u;
        employees = e;
        encoder = p;
        jwt = j;
        mail = m;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = Optional.ofNullable(body.get("username")).orElse("").trim();
        String password = Optional.ofNullable(body.get("password")).orElse("");
        UserAccount u = users.findByUsernameIgnoreCase(username).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (u.getPassword() == null || !encoder.matches(password, u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        String role = normalizeRole(u.getRole());
        Employee e = u.getEmployeeId() == null ? null : employees.findById(u.getEmployeeId()).orElse(null);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("token", jwt.generate(u.getUsername(), role));
        out.put("role", role);
        out.put("employeeId", u.getEmployeeId());
        out.put("email", e != null && e.getEmail() != null ? e.getEmail() : u.getUsername());
        out.put("name", e != null && e.getName() != null ? e.getName() : ("ROLE_ADMIN".equals(role) ? "Administrator" : u.getUsername()));
        return out;
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgot(@RequestBody Map<String, String> body) {
           System.out.println("========== FORGOT PASSWORD API CALLED ==========");
        String email = Optional.ofNullable(body.get("email")).orElse("").trim();
        if (email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        UserAccount account = users.findByUsernameIgnoreCase(email).orElse(null);
        String deliveryEmail = email;
        if (account == null) {
            Employee e = employees.findByEmailIgnoreCase(email).orElse(null);
            if (e != null) {
                account = users.findByEmployeeId(e.getId()).orElse(null);
                deliveryEmail = e.getEmail();
            }
        }
        // Keep account enumeration low-risk; only send when a linked account and real recipient exist.
        if (account != null) {
            final UserAccount target = account;
            String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
            String key = target.getUsername().toLowerCase(Locale.ROOT);
            otps.put(key, new OtpEntry(otp, Instant.now().plusSeconds(300)));
            try {
                MimeMessage message = mail.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
                helper.setTo(deliveryEmail);
                if (sender != null && !sender.isBlank()) {
                    helper.setFrom(sender);
                
                }helper.setSubject("EMS - Password Reset OTP");
                helper.setText("<div style='font-family:Arial,sans-serif;padding:24px;background:#f4f5fb'><div style='max-width:520px;margin:auto;background:#fff;padding:28px;border-radius:16px'><h2 style='color:#31258a'>Employee Management System</h2><p>Your password reset verification code is:</p><div style='font-size:32px;font-weight:bold;letter-spacing:8px;color:#31258a;padding:18px;background:#f0efff;text-align:center;border-radius:10px'>" + otp + "</div><p>This code expires in 5 minutes. If you did not request this, ignore this email.</p></div></div>", true);
                mail.send(message);
            } catch (Exception ex) {
                otps.remove(key);
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OTP email could not be sent. Configure MAIL_USERNAME and MAIL_APP_PASSWORD.");
            }
        }
        return Map.of("message", "If the email is registered, an OTP will be sent.");
    }

    @PostMapping("/verify-otp")
    public Map<String, String> verify(@RequestBody Map<String, String> body) {
        String email = Optional.ofNullable(body.get("email")).orElse("").trim().toLowerCase(Locale.ROOT);
        String code = Optional.ofNullable(body.get("otp")).orElse("");
        UserAccount account = resolveAccount(email);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        
        }String key = account.getUsername().toLowerCase(Locale.ROOT);
        OtpEntry entry = otps.get(key);
        if (entry == null || Instant.now().isAfter(entry.expires()) || !entry.code().equals(code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        
        }String token = UUID.randomUUID().toString();
        resetTokens.put(token, new ResetEntry(key, Instant.now().plusSeconds(600)));
        otps.remove(key);
        return Map.of("message", "OTP verified", "resetToken", token);
    }

    @PostMapping("/reset-password")
    public Map<String, String> reset(@RequestBody Map<String, String> body) {
        String email = Optional.ofNullable(body.get("email")).orElse("").trim().toLowerCase(Locale.ROOT);
        String token = Optional.ofNullable(body.get("resetToken")).orElse("");
        String password = Optional.ofNullable(body.get("newPassword")).orElse("");
        if (password.length() < 8 || password.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be 8 to 72 characters");
        
        }ResetEntry entry = resetTokens.get(token);
        UserAccount account = resolveAccount(email);
        if (entry == null || Instant.now().isAfter(entry.expires()) || account == null || !entry.username().equals(account.getUsername().toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token");
        
        }account.setPassword(encoder.encode(password));
        users.save(account);
        resetTokens.remove(token);
        return Map.of("message", "Password reset successfully");
    }

    private UserAccount resolveAccount(String email) {
        UserAccount u = users.findByUsernameIgnoreCase(email).orElse(null);
        if (u != null) {
            return u;
        
        }Employee e = employees.findByEmailIgnoreCase(email).orElse(null);
        return e == null ? null : users.findByEmployeeId(e.getId()).orElse(null);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_EMPLOYEE";
        
        }String r = role.toUpperCase(Locale.ROOT);
        return r.startsWith("ROLE_") ? r : "ROLE_" + r;
    }
}

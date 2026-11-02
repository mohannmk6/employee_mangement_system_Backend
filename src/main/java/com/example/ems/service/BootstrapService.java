package com.example.ems.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.ems.entity.UserAccount;
import com.example.ems.repository.UserAccountRepository;

@Component
public class BootstrapService implements CommandLineRunner {

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    @Value("${ems.admin.email:admin@ems.local}")
    private String email;
    @Value("${ems.admin.password:ChangeMe123!}")
    private String password;

    public BootstrapService(UserAccountRepository u, PasswordEncoder p) {
        users = u;
        encoder = p;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByUsernameIgnoreCase(email)) {
            UserAccount u = new UserAccount();
            u.setUsername(email);
            u.setPassword(encoder.encode(password));
            u.setRole("ROLE_ADMIN");
            u.setEmployeeId(null);
            users.save(u);
            System.out.println("Created initial admin account: " + email + ". Change the configured password immediately.");
        }
    }
}

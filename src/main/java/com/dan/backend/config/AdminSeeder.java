package com.dan.backend.config;

import com.dan.backend.entity.User;
import com.dan.backend.enums.Role;
import com.dan.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN account from environment variables (ADMIN_EMAIL, ADMIN_PASSWORD) if it
 * does not exist yet. Admins cannot self-register, so this is how you get the first one.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder,
                       @Value("${app.admin.email:}") String email,
                       @Value("${app.admin.password:}") String password,
                       @Value("${app.admin.first-name:System}") String firstName,
                       @Value("${app.admin.last-name:Admin}") String lastName) {
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public void run(String... args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD not set - skipping admin seeding");
            return;
        }
        if (password.length() < 8) {
            log.warn("ADMIN_PASSWORD is shorter than 8 characters - admin was NOT created");
            return;
        }
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmail(normalized)) {
            return;
        }
        User admin = new User();
        admin.setFirstName(firstName);
        admin.setLastName(lastName);
        admin.setEmail(normalized);
        admin.setPassword(encoder.encode(password));
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Created admin account {}", normalized);
    }
}

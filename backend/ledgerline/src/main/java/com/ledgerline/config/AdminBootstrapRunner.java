
package com.ledgerline.config;

import com.ledgerline.entity.AppUser;
import com.ledgerline.enums.UserRole;
import com.ledgerline.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_BOOTSTRAP_ENABLED:false}")
    private boolean enabled;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (!enabled) {
            return;
        }

        String name = System.getenv("ADMIN_BOOTSTRAP_NAME");
        String email = System.getenv("ADMIN_BOOTSTRAP_EMAIL");
        String password = System.getenv("ADMIN_BOOTSTRAP_PASSWORD");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Admin bootstrap is enabled but required environment "
                    + "variables are missing"
            );
        }

        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);

        if (name.length() > 120 || email.length() > 254
                || password.length() < 12) {
            throw new IllegalStateException(
                    "Admin bootstrap details do not meet the required limits"
            );
        }

        var existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            if (existingUser.get().getRole() != UserRole.ADMIN) {
                throw new IllegalStateException(
                        "Bootstrap email already belongs to a non-admin user"
                );
            }

            return;
        }

        AppUser admin = new AppUser(
                name,
                email,
                passwordEncoder.encode(password),
                UserRole.ADMIN
        );

        userRepository.save(admin);
    }
}

package com.diploma.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.diploma.repository.UserRepository;
import com.diploma.enums.Role;
import com.diploma.entities.User;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        userRepository.findByUsername(adminUsername).ifPresentOrElse(
                user -> System.out.println("admin exists"),
                () -> {
                    User adm = new User();
                    adm.setUsername(adminUsername);
                    adm.setRole(Role.ADMIN);
                    adm.setPassword(passwordEncoder.encode(adminPassword));
                    userRepository.save(adm);
                    System.out.println("admin created");
                });
    }
}

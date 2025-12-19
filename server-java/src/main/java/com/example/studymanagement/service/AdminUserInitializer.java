package com.example.studymanagement.service;

import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer {

    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "123456";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureAdminAccount() {
        userRepository.findByUsername(DEFAULT_ADMIN_USERNAME).ifPresentOrElse(user -> {
            if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                user.setRole("ADMIN");
                userRepository.save(user);
            }
        }, () -> {
            User admin = new User();
            admin.setUsername(DEFAULT_ADMIN_USERNAME);
            admin.setNickname("管理员");
            admin.setRole("ADMIN");
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            userRepository.save(admin);
        });
    }
}

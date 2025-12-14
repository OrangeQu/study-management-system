package com.example.studymanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "login_devices")
public class LoginDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    private String deviceKey;
    private String deviceName;
    private String userAgent;
    private String location;
    private String ip;
    private LocalDateTime lastLogin;
    private Boolean active = true;

    @PrePersist
    public void prePersist() {
        lastLogin = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastLogin = LocalDateTime.now();
    }
}

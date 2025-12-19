package com.example.studymanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "study_sessions")
public class StudySession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    private String type; // pomodoro/study
    private Integer durationMinutes;
    private String mode; // work/break
    private Long taskId;
    private Long planId;
    private LocalDateTime startedAt;
    private LocalDateTime createdAt;
    private LocalDateTime endAt;
    private String status; // running/completed/aborted

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (startedAt == null) {
            startedAt = createdAt;
        }
        if (status == null || status.isBlank()) {
            status = "running";
        }
    }
}

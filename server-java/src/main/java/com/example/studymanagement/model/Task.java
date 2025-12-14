package com.example.studymanagement.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String type;
    private Integer priority;
    private LocalDateTime deadline;
    private String course;
    private String status; // todo/doing/done
    private Boolean completed;
    @Column(columnDefinition = "TEXT")
    private String tagsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

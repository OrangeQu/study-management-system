package com.example.studymanagement.repository;

import com.example.studymanagement.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    @Query("select s from StudySession s where s.userId = :userId and s.startedAt between :start and :end")
    List<StudySession> findByUserIdAndRange(Long userId, LocalDateTime start, LocalDateTime end);
}

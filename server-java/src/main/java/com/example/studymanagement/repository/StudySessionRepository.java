package com.example.studymanagement.repository;

import com.example.studymanagement.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    @Query("select s from StudySession s where s.userId = :userId and s.startedAt between :start and :end and (s.status is null or s.status = 'completed')")
    List<StudySession> findCompletedByUserIdAndRange(@Param("userId") Long userId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query("select coalesce(sum(s.durationMinutes), 0) from StudySession s where s.userId = :userId and (s.status is null or s.status = 'completed')")
    long sumTotalMinutes(@Param("userId") Long userId);

    @Query("select coalesce(sum(s.durationMinutes), 0) from StudySession s where s.userId = :userId and s.startedAt between :start and :end and (s.status is null or s.status = 'completed')")
    long sumMinutesInRange(@Param("userId") Long userId,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end);

    @Query("select count(s) from StudySession s where s.userId = :userId and s.startedAt between :start and :end and (s.status is null or s.status = 'completed') and lower(coalesce(s.type,'study')) = 'pomodoro'")
    long countPomodorosInRange(@Param("userId") Long userId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    @Query("select count(s) from StudySession s where s.userId = :userId and (s.status is null or s.status = 'completed') and lower(coalesce(s.type,'study')) = 'pomodoro'")
    long countTotalPomodoros(@Param("userId") Long userId);

    Optional<StudySession> findByIdAndUserId(Long id, Long userId);
}

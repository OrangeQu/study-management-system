package com.example.studymanagement.repository;

import com.example.studymanagement.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByUserId(Long userId, Pageable pageable);
    List<Task> findByUserId(Long userId);

    @Query("select t from Task t where t.userId = :userId and function('DATE', t.deadline) = :date")
    List<Task> findToday(Long userId, LocalDate date);

    List<Task> findByUserIdAndCompletedIsTrue(Long userId);

    List<Task> findByUserIdAndCompletedIsFalseAndDeadlineBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<Task> findByUserIdAndCompletedIsFalse(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCompletedIsTrue(Long userId);
}

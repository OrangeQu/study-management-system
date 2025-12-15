package com.example.studymanagement.repository;

import com.example.studymanagement.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByUserId(Long userId, Pageable pageable);
    List<Task> findByUserId(Long userId);

    @Query("select t from Task t where t.userId = :userId and function('DATE', t.deadline) = :date")
    List<Task> findToday(@Param("userId") Long userId, @Param("date") LocalDate date);

    List<Task> findByUserIdAndCompletedIsTrue(Long userId);

    List<Task> findByUserIdAndCompletedIsFalseAndDeadlineBetween(@Param("userId") Long userId,
                                                                 @Param("start") LocalDateTime start,
                                                                 @Param("end") LocalDateTime end);

    List<Task> findByUserIdAndCompletedIsFalse(Long userId);

    long countByUserId(Long userId);

    long countByUserIdAndCompletedIsTrue(Long userId);
}

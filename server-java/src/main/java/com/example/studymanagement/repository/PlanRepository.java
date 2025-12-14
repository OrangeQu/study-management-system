package com.example.studymanagement.repository;

import com.example.studymanagement.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByUserIdAndDate(Long userId, LocalDate date);
}

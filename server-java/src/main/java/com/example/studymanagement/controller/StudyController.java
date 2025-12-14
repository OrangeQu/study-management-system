package com.example.studymanagement.controller;

import com.example.studymanagement.model.StudySession;
import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.StudySessionRepository;
import com.example.studymanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudySessionRepository studySessionRepository;
    private final TaskRepository taskRepository;

    @PostMapping("/sessions")
    public ResponseEntity<?> record(@AuthenticationPrincipal User user, @RequestBody StudySession session) {
        session.setId(null);
        session.setUserId(user.getId());
        studySessionRepository.save(session);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("id", session.getId())));
    }

    @GetMapping("/stats/today")
    public ResponseEntity<?> today(@AuthenticationPrincipal User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<StudySession> list = studySessionRepository.findByUserIdAndRange(user.getId(), start, end);
        int minutes = list.stream().mapToInt(s -> s.getDurationMinutes() == null ? 0 : s.getDurationMinutes()).sum();
        long pomo = list.stream().filter(s -> "pomodoro".equalsIgnoreCase(s.getType())).count();
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("studyMinutes", minutes, "pomodoroCount", pomo)));
    }

    @GetMapping("/stats/trend")
    public ResponseEntity<?> trend(@AuthenticationPrincipal User user) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(6);
        List<StudySession> sessions = studySessionRepository.findByUserIdAndRange(
            user.getId(), start.atStartOfDay(), today.plusDays(1).atStartOfDay());

        Map<LocalDate, Integer> dailyMinutes = sessions.stream()
            .collect(Collectors.groupingBy(s -> s.getStartedAt().toLocalDate(), Collectors.summingInt(s -> s.getDurationMinutes() == null ? 0 : s.getDurationMinutes())));
        List<Map<String, Object>> daily = java.util.stream.Stream.iterate(start, d -> d.plusDays(1))
            .limit(7)
            .map(date -> {
                Map<String, Object> item = new HashMap<>();
                item.put("date", date.toString());
                item.put("minutes", dailyMinutes.getOrDefault(date, 0));
                return item;
            })
            .collect(Collectors.toList());

        Map<String, Integer> typeDistribution = sessions.stream()
            .collect(Collectors.groupingBy(s -> s.getType() == null ? "study" : s.getType(), Collectors.summingInt(s -> s.getDurationMinutes() == null ? 0 : s.getDurationMinutes())));
        List<Map<String, Object>> distribution = typeDistribution.entrySet().stream()
            .map(e -> {
                Map<String, Object> item = new HashMap<>();
                item.put("name", e.getKey());
                item.put("value", e.getValue());
                return item;
            })
            .collect(Collectors.toList());

        long totalMinutes = sessions.stream().mapToInt(s -> s.getDurationMinutes() == null ? 0 : s.getDurationMinutes()).sum();
        double avgDaily = totalMinutes / 7.0;
        long pomodoroCount = sessions.stream().filter(s -> "pomodoro".equalsIgnoreCase(s.getType())).count();

        long totalTasks = taskRepository.countByUserId(user.getId());
        long completedTasks = taskRepository.countByUserIdAndCompletedIsTrue(user.getId());
        List<Map<String, Object>> completion = new ArrayList<>();
        Map<String, Object> completedMap = new HashMap<>();
        completedMap.put("name", "completed");
        completedMap.put("value", completedTasks);
        completion.add(completedMap);
        Map<String, Object> ongoingMap = new HashMap<>();
        ongoingMap.put("name", "ongoing");
        ongoingMap.put("value", Math.max(0, totalTasks - completedTasks));
        completion.add(ongoingMap);
        int completionRate = totalTasks == 0 ? 0 : (int) Math.round(completedTasks * 100.0 / totalTasks);

        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of(
            "daily", daily,
            "distribution", distribution,
            "completion", completion,
            "total_minutes", totalMinutes,
            "average_minutes", avgDaily,
            "pomodoro_count", pomodoroCount,
            "completion_rate", completionRate
        )));
    }
}

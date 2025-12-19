package com.example.studymanagement.controller;

import com.example.studymanagement.model.StudySession;
import com.example.studymanagement.model.User;
import com.example.studymanagement.model.UserSettings;
import com.example.studymanagement.repository.StudySessionRepository;
import com.example.studymanagement.repository.TaskRepository;
import com.example.studymanagement.repository.UserSettingsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudySessionRepository studySessionRepository;
    private final TaskRepository taskRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/sessions/start")
    public ResponseEntity<?> start(@AuthenticationPrincipal User user, @RequestBody Map<String, Object> payload) {
        StudySession session = new StudySession();
        session.setUserId(user.getId());
        session.setType((String) payload.getOrDefault("type", "pomodoro"));
        session.setMode((String) payload.getOrDefault("mode", "work"));
        session.setDurationMinutes(parseInt(payload.get("duration_minutes"), 25));
        session.setStatus("running");
        LocalDateTime now = LocalDateTime.now();
        session.setStartedAt(now);
        session.setEndAt(now.plusMinutes(session.getDurationMinutes()));
        studySessionRepository.save(session);
        return ResponseEntity.ok(Map.of(
            "code", 0,
            "message", "ok",
            "data", Map.of(
                "id", session.getId(),
                "started_at", session.getStartedAt(),
                "end_at", session.getEndAt()
            )
        ));
    }

    @PatchMapping("/sessions/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal User user,
                                    @PathVariable Long id,
                                    @RequestParam String action,
                                    @RequestBody(required = false) Map<String, Object> payload) {
        StudySession session = studySessionRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "会话不存在"));
        String normalized = action == null ? "" : action.toLowerCase();
        if ("complete".equals(normalized)) {
            session.setStatus("completed");
            session.setMode(payload != null && payload.get("mode") != null ? payload.get("mode").toString() : session.getMode());
            session.setType(payload != null && payload.get("type") != null ? payload.get("type").toString() : session.getType());
            session.setDurationMinutes(parseInt(payload == null ? null : payload.get("duration_minutes"), session.getDurationMinutes()));
            session.setEndAt(LocalDateTime.now());
            studySessionRepository.save(session);
        } else if ("abort".equals(normalized)) {
            session.setStatus("aborted");
            session.setEndAt(LocalDateTime.now());
            studySessionRepository.save(session);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "非法操作");
        }
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("status", session.getStatus())));
    }

    @PostMapping("/sessions")
    public ResponseEntity<?> record(@AuthenticationPrincipal User user, @RequestBody StudySession session) {
        session.setId(null);
        session.setUserId(user.getId());
        if (session.getStatus() == null || session.getStatus().isBlank()) {
            session.setStatus("completed");
        }
        if (session.getEndAt() == null) {
            session.setEndAt(LocalDateTime.now());
        }
        studySessionRepository.save(session);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("id", session.getId())));
    }

    @GetMapping("/stats/today")
    public ResponseEntity<?> today(@AuthenticationPrincipal User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<StudySession> list = studySessionRepository.findCompletedByUserIdAndRange(user.getId(), start, end);
        int minutes = list.stream().mapToInt(s -> s.getDurationMinutes() == null ? 0 : s.getDurationMinutes()).sum();
        long pomo = list.stream().filter(s -> "pomodoro".equalsIgnoreCase(s.getType())).count();
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("studyMinutes", minutes, "pomodoroCount", pomo)));
    }

    @GetMapping("/stats/trend")
    public ResponseEntity<?> trend(@AuthenticationPrincipal User user) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        List<StudySession> sessions = studySessionRepository.findCompletedByUserIdAndRange(
            user.getId(), start.atStartOfDay(), end.plusDays(1).atStartOfDay());

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

    @GetMapping("/stats/overview")
    public ResponseEntity<?> overview(@AuthenticationPrincipal User user) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        LocalDate weekStartDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStart.plusDays(7);
        long todayMinutes = studySessionRepository.sumMinutesInRange(user.getId(), startOfDay, endOfDay);
        long weekMinutes = studySessionRepository.sumMinutesInRange(user.getId(), weekStart, weekEnd);
        long totalMinutes = studySessionRepository.sumTotalMinutes(user.getId());
        long todayPomodoro = studySessionRepository.countPomodorosInRange(user.getId(), startOfDay, endOfDay);
        long totalPomodoro = studySessionRepository.countTotalPomodoros(user.getId());
        long todayTaskCount = taskRepository.findToday(user.getId(), today).size();
        long completedTaskCount = taskRepository.countByUserIdAndCompletedIsTrue(user.getId());

        Map<String, Long> goals = loadGoalMinutes(user.getId());
        long dailyGoalMinutes = goals.getOrDefault("daily", 0L);
        long weeklyGoalMinutes = goals.getOrDefault("weekly", 0L);

        Map<String, Object> data = new HashMap<>();
        data.put("today_minutes", todayMinutes);
        data.put("week_minutes", weekMinutes);
        data.put("total_minutes", totalMinutes);
        data.put("today_goal_minutes", dailyGoalMinutes);
        data.put("week_goal_minutes", weeklyGoalMinutes);
        data.put("today_goal_completed", dailyGoalMinutes == 0 || todayMinutes >= dailyGoalMinutes);
        data.put("week_goal_completed", weeklyGoalMinutes == 0 || weekMinutes >= weeklyGoalMinutes);
        data.put("today_pomodoro_count", todayPomodoro);
        data.put("total_pomodoro_count", totalPomodoro);
        data.put("today_task_count", todayTaskCount);
        data.put("completed_task_count", completedTaskCount);

        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", data));
    }

    private Map<String, Long> loadGoalMinutes(Long userId) {
        Optional<UserSettings> settingsOpt = userSettingsRepository.findByUserId(userId);
        Map<String, Object> prefs = settingsOpt
            .map(UserSettings::getPreferencesJson)
            .map(json -> {
                try {
                    return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    return Map.<String, Object>of();
                }
            }).orElse(Map.of());
        Object studySettings = prefs.get("studySettings");
        Map<String, Object> studyMap = studySettings instanceof Map ? (Map<String, Object>) studySettings : Map.of();
        long dailyGoal = parseLong(studyMap.get("dailyGoal"), 4L) * 60;
        long weeklyGoal = parseLong(studyMap.get("weeklyGoal"), 20L) * 60;
        Map<String, Long> result = new HashMap<>();
        result.put("daily", dailyGoal);
        result.put("weekly", weeklyGoal);
        return result;
    }

    private int parseInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long parseLong(Object value, long defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

package com.example.studymanagement.controller;

import com.example.studymanagement.model.Task;
import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;

    @GetMapping
    public ResponseEntity<?> list(
        @AuthenticationPrincipal User user,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
        @RequestParam(name = "status", required = false) String status,
        @RequestParam(name = "priority", required = false) Integer priority,
        @RequestParam(name = "type", required = false) String type,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "date", required = false) String date,
        @RequestParam(name = "startDate", required = false) String startDate,
        @RequestParam(name = "endDate", required = false) String endDate
    ) {
        List<Task> tasks = taskRepository.findByUserId(user.getId());
        LocalDate filterDate = parseDate(date);
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);

        List<Task> filtered = tasks.stream()
            .filter(t -> status == null || status.isBlank() || status.equalsIgnoreCase(t.getStatus()))
            .filter(t -> priority == null || (t.getPriority() != null && t.getPriority().equals(priority)))
            .filter(t -> type == null || type.isBlank() || type.equalsIgnoreCase(t.getType()))
            .filter(t -> keyword == null || keyword.isBlank() || matchesKeyword(t, keyword))
            .filter(t -> filterDate == null || (t.getDeadline() != null && t.getDeadline().toLocalDate().equals(filterDate)))
            .filter(t -> start == null || (t.getDeadline() != null && !t.getDeadline().toLocalDate().isBefore(start)))
            .filter(t -> end == null || (t.getDeadline() != null && !t.getDeadline().toLocalDate().isAfter(end)))
            .sorted(Comparator.comparing(Task::getDeadline, Comparator.nullsLast(LocalDateTime::compareTo)))
            .collect(Collectors.toList());

        int pageIndex = Math.max(page - 1, 0);
        int fromIndex = Math.min(pageIndex * pageSize, filtered.size());
        int toIndex = Math.min(fromIndex + pageSize, filtered.size());
        List<Task> pageList = filtered.subList(fromIndex, toIndex);

        return ResponseEntity.ok(Map.of(
            "code", 0,
            "message", "ok",
            "data", Map.of("list", pageList, "total", filtered.size())
        ));
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal User user, @RequestBody Task task) {
        task.setId(null);
        task.setUserId(user.getId());
        if (task.getCompleted() == null) {
            task.setCompleted(false);
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus(task.getCompleted() ? "done" : "todo");
        }
        taskRepository.save(task);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("id", task.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody Task req) {
        Task task = taskRepository.findById(id).orElseThrow();
        if (!task.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限", "data", null));
        }
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setPriority(req.getPriority());
        task.setType(req.getType());
        task.setCourse(req.getCourse());
        task.setDeadline(req.getDeadline());
        task.setStatus(req.getStatus());
        task.setCompleted(Boolean.TRUE.equals(req.getCompleted()));
        taskRepository.save(task);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> patchStatus(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Task task = taskRepository.findById(id).orElseThrow();
        if (!task.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限", "data", null));
        }
        task.setStatus((String) body.getOrDefault("status", task.getStatus()));
        Object completed = body.get("completed");
        if (completed != null) {
            task.setCompleted(Boolean.TRUE.equals(completed) || "true".equals(String.valueOf(completed)));
        }
        taskRepository.save(task);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        taskRepository.findById(id)
            .filter(task -> task.getUserId().equals(user.getId()))
            .ifPresent(taskRepository::delete);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @DeleteMapping("/completed")
    public ResponseEntity<?> deleteCompleted(@AuthenticationPrincipal User user) {
        taskRepository.findByUserIdAndCompletedIsTrue(user.getId()).forEach(taskRepository::delete);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<?> summary(@AuthenticationPrincipal User user) {
        LocalDate today = LocalDate.now();
        List<Task> allTasks = taskRepository.findByUserId(user.getId());
        List<Task> todayTasks = taskRepository.findToday(user.getId(), today);
        List<Task> urgent = taskRepository.findByUserIdAndCompletedIsFalseAndDeadlineBetween(
            user.getId(), LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        long total = allTasks.size();
        long done = allTasks.stream().filter(task -> Boolean.TRUE.equals(task.getCompleted())).count();
        long doing = allTasks.stream().filter(t -> "doing".equalsIgnoreCase(t.getStatus())).count();
        long todo = allTasks.stream().filter(t -> !Boolean.TRUE.equals(t.getCompleted())).count();
        long overdue = allTasks.stream()
            .filter(t -> !Boolean.TRUE.equals(t.getCompleted()))
            .filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(LocalDateTime.now()))
            .count();
        int completionRate = total == 0 ? 0 : (int) Math.round(done * 100.0 / total);

        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of(
            "today_todo", todayTasks,
            "urgent", urgent,
            "completed_count", done,
            "total_count", total,
            "completion_rate", completionRate,
            "doing_count", doing,
            "todo_count", todo,
            "overdue_count", overdue
        )));
    }

    private boolean matchesKeyword(Task task, String keyword) {
        String key = keyword.toLowerCase();
        return (task.getTitle() != null && task.getTitle().toLowerCase().contains(key))
            || (task.getDescription() != null && task.getDescription().toLowerCase().contains(key))
            || (task.getCourse() != null && task.getCourse().toLowerCase().contains(key));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}

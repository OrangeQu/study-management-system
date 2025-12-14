package com.example.studymanagement.controller;

import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.GradeRepository;
import com.example.studymanagement.repository.StudySessionRepository;
import com.example.studymanagement.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataController {

    private final TaskRepository taskRepository;
    private final GradeRepository gradeRepository;
    private final StudySessionRepository studySessionRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> export(@AuthenticationPrincipal User user,
                                                    @RequestParam(defaultValue = "all") String type) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        if ("all".equalsIgnoreCase(type) || "tasks".equalsIgnoreCase(type)) {
            payload.put("tasks", taskRepository.findByUserId(user.getId()));
        }
        if ("all".equalsIgnoreCase(type) || "grades".equalsIgnoreCase(type)) {
            payload.put("grades", gradeRepository.findAll().stream()
                .filter(g -> g.getUserId().equals(user.getId()))
                .collect(Collectors.toList()));
        }
        if ("all".equalsIgnoreCase(type) || "study".equalsIgnoreCase(type)) {
            payload.put("study_sessions", studySessionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(user.getId()))
                .collect(Collectors.toList()));
        }
        if (payload.isEmpty()) {
            payload.put("message", "无可导出的数据");
        }
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(bytes);
        String fileName = "export-" + type + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".json";
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.APPLICATION_JSON)
            .contentLength(bytes.length)
            .body(resource);
    }

    @DeleteMapping("/history")
    public ResponseEntity<?> clearHistory(@AuthenticationPrincipal User user,
                                          @RequestParam(defaultValue = "study") String type) {
        int cleared;
        if ("tasks".equalsIgnoreCase(type)) {
            List<com.example.studymanagement.model.Task> doneTasks = taskRepository.findByUserIdAndCompletedIsTrue(user.getId());
            cleared = doneTasks.size();
            doneTasks.forEach(taskRepository::delete);
        } else if ("all".equalsIgnoreCase(type)) {
            List<com.example.studymanagement.model.Task> doneTasks = taskRepository.findByUserIdAndCompletedIsTrue(user.getId());
            doneTasks.forEach(taskRepository::delete);
            var sessions = studySessionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(user.getId()))
                .collect(Collectors.toList());
            studySessionRepository.deleteAll(sessions);
            cleared = doneTasks.size() + sessions.size();
        } else {
            var sessions = studySessionRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(user.getId()))
                .collect(Collectors.toList());
            cleared = sessions.size();
            studySessionRepository.deleteAll(sessions);
        }
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("cleared", cleared, "type", type)));
    }
}

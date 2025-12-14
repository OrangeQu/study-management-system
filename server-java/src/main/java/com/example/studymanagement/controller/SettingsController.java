package com.example.studymanagement.controller;

import com.example.studymanagement.model.User;
import com.example.studymanagement.model.UserSettings;
import com.example.studymanagement.repository.UserSettingsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserSettingsRepository settingsRepository;
    private final ObjectMapper objectMapper;

    @GetMapping("/preferences")
    public ResponseEntity<?> get(@AuthenticationPrincipal User user) {
        UserSettings settings = settingsRepository.findByUserId(user.getId()).orElse(null);
        Map<String, Object> data = settings == null ? defaultPreferences() : parse(settings.getPreferencesJson());
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", data));
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> save(@AuthenticationPrincipal User user, @RequestBody Map<String, Object> prefs) {
        UserSettings settings = settingsRepository.findByUserId(user.getId()).orElseGet(() -> {
            UserSettings s = new UserSettings();
            s.setUserId(user.getId());
            return s;
        });
        try {
            settings.setPreferencesJson(objectMapper.writeValueAsString(prefs));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "偏好格式不正确", "data", null));
        }
        settingsRepository.save(settings);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", prefs));
    }

    private Map<String, Object> parse(String json) {
        if (json == null || json.isBlank()) {
            return defaultPreferences();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return defaultPreferences();
        }
    }

    private Map<String, Object> defaultPreferences() {
        return Map.of(
            "theme", "blue",
            "fixedHeader", true,
            "showBreadcrumb", true,
            "enableTagsView", true,
            "enableAnimation", true,
            "studySettings", Map.of(
                "pomodoroWork", 25,
                "pomodoroBreak", 5,
                "taskReminder", "1d",
                "dailyGoal", 4,
                "weeklyGoal", 20
            )
        );
    }
}

package com.example.studymanagement.controller;

import com.example.studymanagement.model.Plan;
import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanRepository planRepository;

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal User user, @RequestParam(name = "date") String date) {
        LocalDate d = LocalDate.parse(date);
        List<Plan> list = planRepository.findByUserIdAndDate(user.getId(), d);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", list));
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal User user, @RequestBody Plan plan) {
        plan.setId(null);
        plan.setUserId(user.getId());
        planRepository.save(plan);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("id", plan.getId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal User user, @PathVariable("id") Long id, @RequestBody Plan req) {
        Plan plan = planRepository.findById(id).orElseThrow();
        if (!plan.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限", "data", null));
        }
        req.setId(id);
        req.setUserId(user.getId());
        planRepository.save(req);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> patchStatus(@AuthenticationPrincipal User user, @PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        Plan plan = planRepository.findById(id).orElseThrow();
        if (!plan.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("code", 403, "message", "无权限", "data", null));
        }
        plan.setStatus(body.get("status"));
        planRepository.save(plan);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        Plan plan = planRepository.findById(id).orElse(null);
        if (plan != null && plan.getUserId().equals(user.getId())) {
            planRepository.deleteById(id);
        }
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }
}

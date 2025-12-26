package com.example.studymanagement.controller;

import com.example.studymanagement.dto.AdminUserRequest;
import com.example.studymanagement.dto.ResetPasswordRequest;
import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> listUsers(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
        @RequestParam(value = "q", required = false) String query
    ) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<User> pageResult;
        if (StringUtils.hasText(query)) {
            pageResult = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable);
        } else {
            pageResult = userRepository.findAll(pageable);
        }
        List<Map<String, Object>> payload = pageResult.stream()
            .map(this::sanitize)
            .collect(Collectors.toList());
        Map<String, Object> bodyData = new HashMap<>();
        bodyData.put("list", payload);
        bodyData.put("total", pageResult.getTotalElements());
        bodyData.put("page", safePage);
        return success(bodyData);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody AdminUserRequest req) {
        if (!StringUtils.hasText(req.getUsername())) {
            return failure("用户名不能为空", 400, HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(req.getPassword())) {
            return failure("密码不能为空", 400, HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return failure("用户名已存在", 400, HttpStatus.BAD_REQUEST);
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setRole(normalizeRole(req.getRole()));
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
        return success(sanitize(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody AdminUserRequest req) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return failure("用户不存在", 404, HttpStatus.NOT_FOUND);
        }
        if (StringUtils.hasText(req.getUsername()) && !req.getUsername().equals(user.getUsername())) {
            var conflict = userRepository.findByUsername(req.getUsername());
            if (conflict.isPresent() && !conflict.get().getId().equals(user.getId())) {
                return failure("用户名已存在", 400, HttpStatus.BAD_REQUEST);
            }
            user.setUsername(req.getUsername());
        }
        if (StringUtils.hasText(req.getEmail()) && !req.getEmail().equals(user.getEmail())) {
            var conflict = userRepository.findByEmail(req.getEmail());
            if (conflict.isPresent() && !conflict.get().getId().equals(user.getId())) {
                return failure("邮箱已被使用", 400, HttpStatus.BAD_REQUEST);
            }
            user.setEmail(req.getEmail());
        }
        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (StringUtils.hasText(req.getPassword())) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (StringUtils.hasText(req.getRole())) {
            user.setRole(normalizeRole(req.getRole()));
        }
        userRepository.save(user);
        return success(sanitize(user));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> resetPassword(@PathVariable("id") Long id, @RequestBody ResetPasswordRequest req) {
        if (!StringUtils.hasText(req.getPassword())) {
            return failure("密码不能为空", 400, HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return failure("用户不存在", 404, HttpStatus.NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
        return success(true);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        if (!userRepository.existsById(id)) {
            return failure("用户不存在", 404, HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(id);
        return success(true);
    }

    private Map<String, Object> sanitize(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("email", user.getEmail());
        map.put("role", normalizeRole(user.getRole()).toLowerCase());
        return map;
    }

    private String normalizeRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) return "ADMIN";
        return "USER";
    }

    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("code", 0);
        payload.put("message", "ok");
        payload.put("data", data);
        return ResponseEntity.ok(payload);
    }

    private ResponseEntity<Map<String, Object>> failure(String message, int code, HttpStatus status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("code", code);
        payload.put("message", message);
        payload.put("data", null);
        return ResponseEntity.status(status).body(payload);
    }
}

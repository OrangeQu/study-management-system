package com.example.studymanagement.controller;

import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.UserRepository;
import com.example.studymanagement.service.LoginDeviceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginDeviceService loginDeviceService;

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", sanitize(user)));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User user, @RequestBody ProfileRequest req) {
        user.setNickname(req.getNickname());
        user.setGender(req.getGender());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", sanitize(user)));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User user,
                                            @RequestBody Map<String, String> body) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        if (currentPassword == null || currentPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(errorResponse("密码不能为空"));
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(errorResponse("当前密码错误"));
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@AuthenticationPrincipal User user,
                                          @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "文件不能为空", "data", null));
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "文件不能超过2MB", "data", null));
        }
        String base64 = Base64.getEncoder().encodeToString(file.getBytes());
        String avatar = "data:" + file.getContentType() + ";base64," + base64;
        user.setAvatar(avatar);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", Map.of("avatar", avatar)));
    }

    @GetMapping("/devices")
    public ResponseEntity<?> devices(@AuthenticationPrincipal User user, HttpServletRequest request) {
        return ResponseEntity.ok(Map.of(
            "code", 0,
            "message", "ok",
            "data", loginDeviceService.listDevices(user, request)
        ));
    }

    @DeleteMapping("/devices/{id}")
    public ResponseEntity<?> removeDevice(@AuthenticationPrincipal User user, @PathVariable("id") Long id) {
        loginDeviceService.removeDevice(user.getId(), id);
        return ResponseEntity.ok(Map.of("code", 0, "message", "ok", "data", true));
    }

    private Map<String, Object> sanitize(User user) {
        Map<String, Object> info = new java.util.HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("nickname", user.getNickname());
        info.put("gender", user.getGender());
        info.put("phone", user.getPhone());
        info.put("avatar", user.getAvatar());
        return info;
    }

    @Data
    public static class ProfileRequest {
        private String nickname;
        private String gender;
        private String phone;
        private String email;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("code", 400);
        map.put("message", message);
        map.put("data", null);
        return map;
    }
}

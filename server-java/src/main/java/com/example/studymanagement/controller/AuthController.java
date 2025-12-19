package com.example.studymanagement.controller;

import com.example.studymanagement.dto.AuthResponse;
import com.example.studymanagement.dto.LoginRequest;
import com.example.studymanagement.dto.RegisterRequest;
import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.UserRepository;
import com.example.studymanagement.service.JwtService;
import com.example.studymanagement.service.LoginDeviceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginDeviceService loginDeviceService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            Map<String, Object> duplicate = new HashMap<>();
            duplicate.put("code", 400);
            duplicate.put("message", "用户已存在");
            duplicate.put("data", null);
            return ResponseEntity.badRequest().body(duplicate);
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        userRepository.save(user);
        String token = jwtService.generateToken(user);
        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole() != null ? user.getRole().toLowerCase() : "user");
        resp.setUser(userInfo);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 0);
        body.put("message", "ok");
        body.put("data", resp);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);
        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("role", user.getRole() != null ? user.getRole().toLowerCase() : "user");
        resp.setUser(userInfo);
        loginDeviceService.recordLogin(user, servletRequest);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 0);
        body.put("message", "ok");
        body.put("data", resp);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        Map<String, Object> body = new HashMap<>();
        body.put("code", 0);
        body.put("message", "ok");
        body.put("data", true);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Map<String, Object> err = new HashMap<>();
            err.put("code", 401);
            err.put("message", "未授权");
            err.put("data", null);
            return ResponseEntity.status(401).body(err);
        }
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        String newToken = jwtService.generateToken(user);
        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        Map<String, Object> body = new HashMap<>();
        body.put("code", 0);
        body.put("message", "ok");
        body.put("data", data);
        return ResponseEntity.ok(body);
    }
}

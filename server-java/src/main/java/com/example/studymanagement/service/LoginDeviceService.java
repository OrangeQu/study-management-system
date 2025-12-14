package com.example.studymanagement.service;

import com.example.studymanagement.model.LoginDevice;
import com.example.studymanagement.model.User;
import com.example.studymanagement.repository.LoginDeviceRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginDeviceService {

    private final LoginDeviceRepository loginDeviceRepository;

    public LoginDevice recordLogin(User user, HttpServletRequest request) {
        String deviceKey = buildDeviceKey(request);
        LoginDevice device = loginDeviceRepository.findByUserIdAndDeviceKey(user.getId(), deviceKey)
            .orElseGet(() -> {
                LoginDevice d = new LoginDevice();
                d.setUserId(user.getId());
                d.setDeviceKey(deviceKey);
                return d;
            });
        device.setDeviceName(resolveDeviceName(request.getHeader("User-Agent")));
        device.setUserAgent(request.getHeader("User-Agent"));
        device.setIp(resolveClientIp(request));
        device.setLocation(resolveLocation(request));
        device.setActive(true);
        device.setLastLogin(LocalDateTime.now());
        return loginDeviceRepository.save(device);
    }

    public void removeDevice(Long userId, Long deviceId) {
        loginDeviceRepository.findById(deviceId)
            .filter(device -> device.getUserId().equals(userId))
            .ifPresent(loginDeviceRepository::delete);
    }

    public List<Map<String, Object>> listDevices(User user, HttpServletRequest request) {
        String currentKey = buildDeviceKey(request);
        return loginDeviceRepository.findByUserId(user.getId()).stream()
            .sorted(Comparator.comparing(LoginDevice::getLastLogin).reversed())
            .map(device -> {
                Map<String, Object> item = new HashMap<>();
                item.put("id", device.getId());
                item.put("device_name", device.getDeviceName());
                item.put("location", device.getLocation());
                item.put("last_login", device.getLastLogin());
                item.put("is_current", device.getDeviceKey().equals(currentKey));
                item.put("ip", device.getIp());
                return item;
            })
            .collect(Collectors.toList());
    }

    private String resolveLocation(HttpServletRequest request) {
        String location = request.getHeader("X-Location");
        return location != null ? location : "未知位置";
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String resolveDeviceName(String userAgent) {
        if (userAgent == null) {
            return "未知设备";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("iphone")) return "iPhone";
        if (ua.contains("android")) return "Android";
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os")) return "Mac";
        if (ua.contains("ipad")) return "iPad";
        return "Web 设备";
    }

    public String buildDeviceKey(HttpServletRequest request) {
        String raw = (request.getHeader("User-Agent") == null ? "" : request.getHeader("User-Agent"))
            + "|" + resolveClientIp(request);
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }
}

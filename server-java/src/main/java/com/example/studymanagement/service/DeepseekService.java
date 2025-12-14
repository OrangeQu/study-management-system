package com.example.studymanagement.service;

import com.example.studymanagement.config.DeepseekProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeepseekService {

    private final DeepseekProperties properties;
    private final RestTemplateBuilder restTemplateBuilder;

    public String chat(List<Map<String, Object>> history) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("DeepSeek API Key 未配置");
        }
        RestTemplate restTemplate = restTemplateBuilder
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(60))
            .build();

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", properties.getModel());
        payload.put("temperature", properties.getTemperature());
        if (properties.getMaxTokens() != null) {
            payload.put("max_tokens", properties.getMaxTokens());
        }
        payload.put("messages", buildMessages(history));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            properties.getChatCompletionUrl(), entity, Map.class);

        return extractReply(response.getBody());
    }

    private List<Map<String, String>> buildMessages(List<Map<String, Object>> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", "你是一个贴心的学习助手，请用中文回答并提供具体建议。");
        messages.add(system);

        if (!CollectionUtils.isEmpty(history)) {
            for (Map<String, Object> item : history) {
                String role = String.valueOf(item.getOrDefault("role", "user"));
                String content = String.valueOf(item.getOrDefault("content", ""));
                if (!StringUtils.hasText(content)) {
                    continue;
                }
                Map<String, String> message = new HashMap<>();
                message.put("role", "assistant".equalsIgnoreCase(role) ? "assistant" : "user");
                message.put("content", content);
                messages.add(message);
            }
        }
        return messages;
    }

    @SuppressWarnings("unchecked")
    private String extractReply(Map body) {
        if (body == null) {
            throw new IllegalStateException("DeepSeek 返回为空");
        }
        Object choicesObj = body.get("choices");
        if (choicesObj instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> map) {
                Object messageObj = map.get("message");
                if (messageObj instanceof Map<?, ?> messageMap) {
                    Object content = messageMap.get("content");
                    if (content != null) {
                        return content.toString();
                    }
                }
                Object text = map.get("text"); // 兼容不同返回
                if (text != null) {
                    return text.toString();
                }
            }
        }
        throw new IllegalStateException("无法解析 DeepSeek 响应: " + body);
    }
}

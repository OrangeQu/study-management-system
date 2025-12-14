package com.example.studymanagement.controller;

import com.example.studymanagement.service.DeepseekService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final DeepseekService deepseekService;

    private final Map<String, List<Map<String, Object>>> conversations = new ConcurrentHashMap<>();

    @PostMapping("/messages")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, Object> body) {
        String content = String.valueOf(body.getOrDefault("content", "")).trim();
        if (content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "消息不能为空", "data", null));
        }
        String conversationId = body.getOrDefault("conversation_id", UUID.randomUUID().toString()).toString();
        List<Map<String, Object>> history = conversations.computeIfAbsent(conversationId, k -> new ArrayList<>());
        Map<String, Object> userMsg = Map.of(
            "id", UUID.randomUUID().toString(),
            "role", "user",
            "content", content,
            "time", LocalDateTime.now()
        );
        history.add(userMsg);

        String reply;
        try {
            reply = deepseekService.chat(history);
        } catch (Exception ex) {
            log.error("调用 DeepSeek 失败", ex);
            reply = "抱歉，我暂时无法连接到 AI 服务，请稍后再试。错误信息：" + ex.getMessage();
        }

        Map<String, Object> aiMsg = Map.of(
            "id", UUID.randomUUID().toString(),
            "role", "assistant",
            "content", reply,
            "time", LocalDateTime.now()
        );
        history.add(aiMsg);

        return ResponseEntity.ok(Map.of(
            "code", 0,
            "message", "ok",
            "data", Map.of(
                "conversation_id", conversationId,
                "reply", reply,
                "history", history
            )
        ));
    }
}

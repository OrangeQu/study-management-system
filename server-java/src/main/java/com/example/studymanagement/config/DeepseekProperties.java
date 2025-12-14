package com.example.studymanagement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "deepseek")
public class DeepseekProperties {

    /**
     * HTTP base url, default https://api.deepseek.com
     */
    private String apiBase = "https://api.deepseek.com";

    /**
     * API key for DeepSeek.
     */
    private String apiKey;

    /**
     * Model name, default deepseek-chat
     */
    private String model = "deepseek-chat";

    /**
     * Temperature for sampling.
     */
    private double temperature = 0.8;

    /**
     * Max tokens fallback (optional, DeepSeek can ignore)
     */
    private Integer maxTokens = 2048;

    public String getChatCompletionUrl() {
        String base = apiBase == null ? "https://api.deepseek.com" : apiBase.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/chat/completions";
    }
}

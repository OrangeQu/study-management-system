package com.example.studymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.studymanagement.config.DeepseekProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DeepseekProperties.class)
public class StudyManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyManagementApplication.class, args);
    }
}

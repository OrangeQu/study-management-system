package com.example.studymanagement.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Object user;
}

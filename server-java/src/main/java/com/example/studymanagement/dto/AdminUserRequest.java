package com.example.studymanagement.dto;

import lombok.Data;

@Data
public class AdminUserRequest {
    private String username;
    private String nickname;
    private String email;
    private String password;
    private String role;
}

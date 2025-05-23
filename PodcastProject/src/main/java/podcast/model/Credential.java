package com.example.demo.model;

import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;

@Embeddable
public class Credential {
    private String email;
    private String username;
    private String password;
    private Boolean isVerified = false;
    private LocalDateTime lastLogin;
    private String resetToken;
    private LocalDateTime createdAt;
}
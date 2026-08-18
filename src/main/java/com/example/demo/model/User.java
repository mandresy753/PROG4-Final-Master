package com.example.demo.model;

import com.example.demo.enums.UserRole;
import java.util.UUID;
import lombok.Builder;

@Builder
public record User(
    UUID id,
    String reference,
    String lastName,
    String firstName,
    String email,
    String password,
    UserRole role) {}

package com.example.demo.dto;

public record LoginResponse(
    String token, String userId, String firstName, String lastName, String role) {}

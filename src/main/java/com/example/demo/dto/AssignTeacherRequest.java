package com.example.demo.dto;

import java.util.UUID;

public record AssignTeacherRequest(UUID courseOfferingId, UUID teacherId) {}

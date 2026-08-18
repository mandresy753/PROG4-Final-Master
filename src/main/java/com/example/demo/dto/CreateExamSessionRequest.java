package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateExamSessionRequest(
    UUID examId, LocalDateTime examDate, UUID teacherId, List<UUID> groupIds) {}

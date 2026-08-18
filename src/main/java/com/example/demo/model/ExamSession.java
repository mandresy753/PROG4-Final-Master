package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ExamSession(
    UUID id, Exam exam, LocalDateTime examDate, User teacher, List<Group> groups) {}

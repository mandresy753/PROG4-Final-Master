package com.example.demo.dto;

import com.example.demo.enums.Level;
import java.time.LocalDate;
import java.util.UUID;

public record CreateEnrollmentRequest(
    UUID studentId,
    UUID groupId,
    UUID academicYearId,
    Level level,
    LocalDate startDate,
    LocalDate endDate) {}

package com.example.demo.model;

import com.example.demo.enums.Level;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Enrollment(
    UUID id,
    User student,
    Group group,
    AcademicYear academicYear,
    Level level,
    LocalDate startDate,
    LocalDate endDate) {}

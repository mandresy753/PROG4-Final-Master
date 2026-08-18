package com.example.demo.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record AcademicYear(UUID id, String label, LocalDate startDate, LocalDate endDate) {}

package com.example.demo.model.report;

import com.example.demo.model.AcademicYear;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record YearAverage(
    AcademicYear academicYear,
    List<CourseAverage> courseAverages,
    BigDecimal generalAverage,
    int totalCredits,
    int validatedCredits,
    boolean complete) {}

package com.example.demo.model.transcript;

import com.example.demo.model.AcademicYear;
import com.example.demo.model.User;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record YearTranscript(
    User student,
    AcademicYear academicYear,
    List<TranscriptCourseLine> courses,
    BigDecimal generalAverage,
    int totalCredits,
    int validatedCredits,
    TranscriptStatus status) {}

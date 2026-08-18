package com.example.demo.model.transcript;

import com.example.demo.model.User;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record FullTranscript(
    User student,
    List<YearTranscript> years,
    BigDecimal overallAverage,
    int totalCredits,
    TranscriptStatus status) {}

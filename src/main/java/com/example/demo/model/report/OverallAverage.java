package com.example.demo.model.report;

import com.example.demo.model.User;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record OverallAverage(
    User student,
    List<YearAverage> years,
    BigDecimal overallAverage,
    int totalCredits,
    boolean complete) {}

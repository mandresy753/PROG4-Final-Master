package com.example.demo.model;

import com.example.demo.enums.Track;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record Graduate(
    User student,
    Track track,
    String promotion,
    BigDecimal overallAverage,
    int totalCredits,
    boolean graduated,
    int rank) {}

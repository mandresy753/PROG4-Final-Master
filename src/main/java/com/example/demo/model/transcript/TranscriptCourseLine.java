package com.example.demo.model.transcript;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record TranscriptCourseLine(
    String courseRef,
    String courseTitle,
    int creditCount,
    BigDecimal average,
    boolean validated,
    TranscriptStatus status) {}

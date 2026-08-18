package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordGradeRequest(UUID studentId, BigDecimal value, String reason) {}

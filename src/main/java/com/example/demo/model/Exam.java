package com.example.demo.model;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Exam(UUID id, CourseOffering courseOffering, BigDecimal coefficient) {}

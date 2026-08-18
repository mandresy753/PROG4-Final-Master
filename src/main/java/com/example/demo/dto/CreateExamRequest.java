package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateExamRequest(UUID courseOfferingId, BigDecimal coefficient) {}

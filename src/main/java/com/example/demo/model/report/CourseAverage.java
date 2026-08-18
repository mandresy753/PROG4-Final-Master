package com.example.demo.model.report;

import com.example.demo.model.CourseOffering;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record CourseAverage(
    CourseOffering courseOffering,
    BigDecimal average,
    boolean complete,
    BigDecimal gradedCoefficientSum) {}

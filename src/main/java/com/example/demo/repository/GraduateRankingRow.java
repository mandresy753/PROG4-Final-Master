package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface GraduateRankingRow {

  UUID getId();

  String getReference();

  String getLastName();

  String getFirstName();

  String getEmail();

  BigDecimal getOverallAverage();

  Integer getTotalCredits();

  Integer getRank();
}

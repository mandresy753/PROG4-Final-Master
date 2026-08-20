package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.enums.Track;
import com.example.demo.model.Graduate;
import com.example.demo.model.User;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduateXlsxGeneratorTest {

  @InjectMocks private GraduateXlsxGenerator graduateXlsxGenerator;

  @Test
  void generate_empty() {
    var file = graduateXlsxGenerator.generate(Map.of(Track.EL, List.of(), Track.TN, List.of()));
    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.getName().endsWith(".xlsx"));
    file.delete();
  }

  @Test
  void generate_withGraduates() {
    var graduate =
        Graduate.builder()
            .student(
                User.builder().reference("STD0001").lastName("Rakoto").firstName("Jean").build())
            .track(Track.EL)
            .promotion("2025-2026")
            .overallAverage(new BigDecimal("14.50"))
            .totalCredits(180)
            .graduated(true)
            .rank(1)
            .build();
    var file =
        graduateXlsxGenerator.generate(Map.of(Track.EL, List.of(graduate), Track.TN, List.of()));
    assertNotNull(file);
    assertTrue(file.exists());
    assertTrue(file.length() > 0);
    file.delete();
  }
}

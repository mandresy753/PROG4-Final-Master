package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.Track;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.GraduateRankingRow;
import com.example.demo.repository.GraduationQueryRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduationServiceTest {

  @Mock private GraduationQueryRepository graduationQueryRepository;
  @Mock private EnrollmentRepository enrollmentRepository;
  @InjectMocks private GraduationService graduationService;

  @Test
  void listPromotions() {
    when(enrollmentRepository.findDistinctAcademicYear_LabelByLevelOrderByAcademicYear_LabelDesc(
            any()))
        .thenReturn(List.of("2025-2026", "2024-2025"));

    var result = graduationService.listPromotions();
    assertEquals(2, result.size());
    assertEquals("2025-2026", result.get(0));
  }

  @Test
  void listGraduates() {
    var row = mock(GraduateRankingRow.class);
    when(row.getId()).thenReturn(java.util.UUID.randomUUID());
    when(row.getReference()).thenReturn("STD0001");
    when(row.getLastName()).thenReturn("Rakoto");
    when(row.getFirstName()).thenReturn("Jean");
    when(row.getEmail()).thenReturn("jean@test.com");
    when(row.getOverallAverage()).thenReturn(new BigDecimal("14.50"));
    when(row.getTotalCredits()).thenReturn(180);
    when(row.getRank()).thenReturn(1);

    when(graduationQueryRepository.findRankedGraduates(
            "EL", "2025-2026", GraduationService.EXPECTED_TOTAL_CREDITS))
        .thenReturn(List.of(row));

    var result = graduationService.listGraduates(Track.EL, "2025-2026");

    assertEquals(1, result.size());
    assertEquals(1, result.get(0).rank());
    assertEquals("Rakoto", result.get(0).student().lastName());
  }

  @Test
  void listGraduatesByPromotion() {
    when(graduationQueryRepository.findRankedGraduates(eq("EL"), anyString(), anyInt()))
        .thenReturn(List.of());
    when(graduationQueryRepository.findRankedGraduates(eq("TN"), anyString(), anyInt()))
        .thenReturn(List.of());

    var result = graduationService.listGraduatesByPromotion("2025-2026");

    assertEquals(2, result.size());
    assertTrue(result.containsKey(Track.EL));
    assertTrue(result.containsKey(Track.TN));
  }

  @Test
  void listGraduates_empty() {
    when(graduationQueryRepository.findRankedGraduates(anyString(), anyString(), anyInt()))
        .thenReturn(List.of());

    assertTrue(graduationService.listGraduates(Track.EL, "2025-2026").isEmpty());
  }
}

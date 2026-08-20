package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.UserRole;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.report.CourseAverage;
import com.example.demo.model.report.OverallAverage;
import com.example.demo.model.report.YearAverage;
import com.example.demo.model.transcript.TranscriptStatus;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptServiceTest {

  @Mock private GradeAverageService gradeAverageService;
  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @InjectMocks private TranscriptService transcriptService;

  @Test
  void yearTranscript() {
    var studentId = UUID.randomUUID();
    var yearId = UUID.randomUUID();
    var student =
        User.builder()
            .id(studentId)
            .lastName("Rakoto")
            .firstName("Jean")
            .role(UserRole.STUDENT)
            .build();
    when(userRepository.findById(studentId))
        .thenReturn(
            Optional.of(
                com.example.demo.entity.JUser.builder()
                    .id(studentId)
                    .lastName("Rakoto")
                    .firstName("Jean")
                    .role(UserRole.STUDENT)
                    .build()));
    when(userMapper.toModel(any())).thenReturn(student);

    var courseAvg =
        CourseAverage.builder()
            .courseOffering(
                com.example.demo.model.CourseOffering.builder()
                    .course(
                        com.example.demo.model.Course.builder()
                            .ref("PROG1")
                            .title("Programmation 1")
                            .creditCount(6)
                            .build())
                    .build())
            .average(new BigDecimal("14"))
            .complete(true)
            .gradedCoefficientSum(BigDecimal.ONE)
            .build();
    var yearAvg =
        YearAverage.builder()
            .academicYear(com.example.demo.model.AcademicYear.builder().label("2025-2026").build())
            .courseAverages(List.of(courseAvg))
            .generalAverage(new BigDecimal("14"))
            .totalCredits(6)
            .validatedCredits(6)
            .complete(true)
            .build();
    when(gradeAverageService.yearAverage(studentId, yearId)).thenReturn(yearAvg);

    var result = transcriptService.yearTranscript(studentId, yearId);

    assertNotNull(result);
    assertEquals(TranscriptStatus.COMPLETE, result.status());
    assertEquals(1, result.courses().size());
  }

  @Test
  void fullTranscript() {
    var studentId = UUID.randomUUID();
    var student =
        User.builder()
            .id(studentId)
            .lastName("Rakoto")
            .firstName("Jean")
            .role(UserRole.STUDENT)
            .build();

    var courseAvg =
        CourseAverage.builder()
            .courseOffering(
                com.example.demo.model.CourseOffering.builder()
                    .course(
                        com.example.demo.model.Course.builder()
                            .ref("PROG1")
                            .title("Programmation 1")
                            .creditCount(6)
                            .build())
                    .build())
            .average(new BigDecimal("14"))
            .complete(true)
            .gradedCoefficientSum(BigDecimal.ONE)
            .build();
    var yearAvg =
        YearAverage.builder()
            .academicYear(com.example.demo.model.AcademicYear.builder().label("2025-2026").build())
            .courseAverages(List.of(courseAvg))
            .generalAverage(new BigDecimal("14"))
            .totalCredits(6)
            .validatedCredits(6)
            .complete(true)
            .build();
    var overallAvg =
        OverallAverage.builder()
            .student(student)
            .years(List.of(yearAvg))
            .overallAverage(new BigDecimal("14"))
            .totalCredits(6)
            .complete(true)
            .build();
    when(gradeAverageService.overallAverage(studentId)).thenReturn(overallAvg);

    var result = transcriptService.fullTranscript(studentId);

    assertNotNull(result);
    assertEquals(TranscriptStatus.COMPLETE, result.status());
    assertEquals(1, result.years().size());
  }

  @Test
  void fullTranscript_provisional() {
    var studentId = UUID.randomUUID();
    var student =
        User.builder()
            .id(studentId)
            .lastName("Rakoto")
            .firstName("Jean")
            .role(UserRole.STUDENT)
            .build();

    var courseAvg =
        CourseAverage.builder()
            .courseOffering(
                com.example.demo.model.CourseOffering.builder()
                    .course(
                        com.example.demo.model.Course.builder()
                            .ref("PROG1")
                            .title("Programmation 1")
                            .creditCount(6)
                            .build())
                    .build())
            .average(null)
            .complete(false)
            .gradedCoefficientSum(BigDecimal.ZERO)
            .build();
    var yearAvg =
        YearAverage.builder()
            .academicYear(com.example.demo.model.AcademicYear.builder().label("2025-2026").build())
            .courseAverages(List.of(courseAvg))
            .generalAverage(null)
            .totalCredits(6)
            .validatedCredits(0)
            .complete(false)
            .build();
    var overallAvg =
        OverallAverage.builder()
            .student(student)
            .years(List.of(yearAvg))
            .overallAverage(null)
            .totalCredits(6)
            .complete(false)
            .build();
    when(gradeAverageService.overallAverage(studentId)).thenReturn(overallAvg);

    var result = transcriptService.fullTranscript(studentId);

    assertEquals(TranscriptStatus.PROVISIONAL, result.status());
  }

  @Test
  void yearTranscript_studentNotFound() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    when(gradeAverageService.yearAverage(any(), any()))
        .thenReturn(
            YearAverage.builder()
                .academicYear(null)
                .courseAverages(List.of())
                .complete(true)
                .totalCredits(0)
                .validatedCredits(0)
                .build());
    assertThrows(
        ResourceNotFoundException.class,
        () -> transcriptService.yearTranscript(UUID.randomUUID(), UUID.randomUUID()));
  }
}

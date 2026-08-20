package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.enums.UserRole;
import com.example.demo.model.AcademicYear;
import com.example.demo.model.User;
import com.example.demo.model.transcript.FullTranscript;
import com.example.demo.model.transcript.TranscriptCourseLine;
import com.example.demo.model.transcript.TranscriptStatus;
import com.example.demo.model.transcript.YearTranscript;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptPdfGeneratorTest {

  @InjectMocks private TranscriptPdfGenerator transcriptPdfGenerator;

  @Test
  void generate() {
    var student =
        User.builder()
            .id(java.util.UUID.randomUUID())
            .lastName("Rakoto")
            .firstName("Jean")
            .email("jean@test.com")
            .role(UserRole.STUDENT)
            .build();

    var courseLine =
        TranscriptCourseLine.builder()
            .courseRef("PROG1")
            .courseTitle("Programmation 1")
            .creditCount(6)
            .average(new BigDecimal("14"))
            .validated(true)
            .status(TranscriptStatus.COMPLETE)
            .build();

    var yearTranscript =
        YearTranscript.builder()
            .student(student)
            .academicYear(AcademicYear.builder().label("2025-2026").build())
            .courses(List.of(courseLine))
            .generalAverage(new BigDecimal("14"))
            .totalCredits(6)
            .validatedCredits(6)
            .status(TranscriptStatus.COMPLETE)
            .build();

    var fullTranscript =
        FullTranscript.builder()
            .student(student)
            .years(List.of(yearTranscript))
            .overallAverage(new BigDecimal("14"))
            .totalCredits(6)
            .status(TranscriptStatus.COMPLETE)
            .build();

    File result = transcriptPdfGenerator.generate(fullTranscript);

    assertNotNull(result);
    assertTrue(result.exists());
    assertTrue(result.getName().endsWith(".pdf"));
    assertTrue(result.length() > 0);
    result.delete();
  }

  @Test
  void generate_multipleYears() {
    var student =
        User.builder()
            .id(java.util.UUID.randomUUID())
            .lastName("Rakoto")
            .firstName("Jean")
            .email("jean@test.com")
            .role(UserRole.STUDENT)
            .build();

    var courseLine1 =
        TranscriptCourseLine.builder()
            .courseRef("PROG1")
            .courseTitle("Programmation 1")
            .creditCount(6)
            .average(new BigDecimal("14"))
            .validated(true)
            .status(TranscriptStatus.COMPLETE)
            .build();
    var courseLine2 =
        TranscriptCourseLine.builder()
            .courseRef("WEB1")
            .courseTitle("Web 1")
            .creditCount(6)
            .average(new BigDecimal("12"))
            .validated(true)
            .status(TranscriptStatus.COMPLETE)
            .build();

    var year1 =
        YearTranscript.builder()
            .student(student)
            .academicYear(AcademicYear.builder().label("2024-2025").build())
            .courses(List.of(courseLine1))
            .generalAverage(new BigDecimal("14"))
            .totalCredits(30)
            .validatedCredits(30)
            .status(TranscriptStatus.COMPLETE)
            .build();

    var year2 =
        YearTranscript.builder()
            .student(student)
            .academicYear(AcademicYear.builder().label("2025-2026").build())
            .courses(List.of(courseLine2))
            .generalAverage(new BigDecimal("12"))
            .totalCredits(30)
            .validatedCredits(30)
            .status(TranscriptStatus.COMPLETE)
            .build();

    var fullTranscript =
        FullTranscript.builder()
            .student(student)
            .years(List.of(year1, year2))
            .overallAverage(new BigDecimal("13"))
            .totalCredits(60)
            .status(TranscriptStatus.COMPLETE)
            .build();

    File result = transcriptPdfGenerator.generate(fullTranscript);

    assertNotNull(result);
    assertTrue(result.exists());
    result.delete();
  }

  @Test
  void generate_withNullAverages() {
    var student =
        User.builder()
            .id(java.util.UUID.randomUUID())
            .lastName("Rakoto")
            .firstName("Jean")
            .email("jean@test.com")
            .role(UserRole.STUDENT)
            .build();

    var courseLine =
        TranscriptCourseLine.builder()
            .courseRef("PROG1")
            .courseTitle("Programmation 1")
            .creditCount(6)
            .average(null)
            .validated(false)
            .status(TranscriptStatus.PROVISIONAL)
            .build();

    var yearTranscript =
        YearTranscript.builder()
            .student(student)
            .academicYear(AcademicYear.builder().label("2025-2026").build())
            .courses(List.of(courseLine))
            .generalAverage(null)
            .totalCredits(6)
            .validatedCredits(0)
            .status(TranscriptStatus.PROVISIONAL)
            .build();

    var fullTranscript =
        FullTranscript.builder()
            .student(student)
            .years(List.of(yearTranscript))
            .overallAverage(null)
            .totalCredits(6)
            .status(TranscriptStatus.PROVISIONAL)
            .build();

    File result = transcriptPdfGenerator.generate(fullTranscript);

    assertNotNull(result);
    assertTrue(result.exists());
    result.delete();
  }
}

package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.model.report.CourseAverage;
import com.example.demo.model.report.YearAverage;
import com.example.demo.model.transcript.FullTranscript;
import com.example.demo.model.transcript.TranscriptCourseLine;
import com.example.demo.model.transcript.TranscriptStatus;
import com.example.demo.model.transcript.YearTranscript;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TranscriptService {

  private final GradeAverageService gradeAverageService;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public YearTranscript yearTranscript(UUID studentId, UUID academicYearId) {
    var yearAverage = gradeAverageService.yearAverage(studentId, academicYearId);
    var student = loadStudent(studentId);
    return toYearTranscript(student, yearAverage);
  }

  @Transactional(readOnly = true)
  public FullTranscript fullTranscript(UUID studentId) {
    var overallAverage = gradeAverageService.overallAverage(studentId);

    var years =
        overallAverage.years().stream()
            .map(yearAverage -> toYearTranscript(overallAverage.student(), yearAverage))
            .toList();

    return FullTranscript.builder()
        .student(overallAverage.student())
        .years(years)
        .overallAverage(overallAverage.overallAverage())
        .totalCredits(overallAverage.totalCredits())
        .status(
            overallAverage.complete() ? TranscriptStatus.COMPLETE : TranscriptStatus.PROVISIONAL)
        .build();
  }

  private YearTranscript toYearTranscript(User student, YearAverage yearAverage) {
    var courses = yearAverage.courseAverages().stream().map(this::toCourseLine).toList();

    return YearTranscript.builder()
        .student(student)
        .academicYear(yearAverage.academicYear())
        .courses(courses)
        .generalAverage(yearAverage.generalAverage())
        .totalCredits(yearAverage.totalCredits())
        .validatedCredits(yearAverage.validatedCredits())
        .status(yearAverage.complete() ? TranscriptStatus.COMPLETE : TranscriptStatus.PROVISIONAL)
        .build();
  }

  private TranscriptCourseLine toCourseLine(CourseAverage courseAverage) {
    var course = courseAverage.courseOffering().course();
    var validated =
        courseAverage.average() != null && courseAverage.average().compareTo(BigDecimal.TEN) >= 0;

    return TranscriptCourseLine.builder()
        .courseRef(course.ref())
        .courseTitle(course.title())
        .creditCount(course.creditCount())
        .average(courseAverage.average())
        .validated(validated)
        .status(courseAverage.complete() ? TranscriptStatus.COMPLETE : TranscriptStatus.PROVISIONAL)
        .build();
  }

  private User loadStudent(UUID studentId) {
    return userRepository
        .findById(studentId)
        .map(userMapper::toModel)
        .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
  }
}

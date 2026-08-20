package com.example.demo.service;

import com.example.demo.entity.JGrade;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.GradeMapper;
import com.example.demo.model.Grade;
import com.example.demo.repository.ExamSessionRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GradeService {

  private final GradeRepository gradeRepository;
  private final ExamSessionRepository examSessionRepository;
  private final UserRepository userRepository;
  private final GradeMapper gradeMapper;

  public List<Grade> history(UUID examSessionId, UUID studentId) {
    return gradeRepository
        .findByExamSession_IdAndStudent_IdOrderByEntryDateDesc(examSessionId, studentId)
        .stream()
        .map(gradeMapper::toModel)
        .toList();
  }

  public List<Grade> currentGradesForStudent(UUID studentId) {
    var all = gradeRepository.findByStudent_Id(studentId);

    var byExam =
        all.stream()
            .collect(
                java.util.stream.Collectors.groupingBy(g -> g.getExamSession().getExam().getId()));

    return byExam.values().stream()
        .map(list -> list.stream().max(Comparator.comparing(JGrade::getEntryDate)).orElseThrow())
        .map(gradeMapper::toModel)
        .toList();
  }

  public Grade record(
      UUID examSessionId, UUID studentId, UUID enteredById, BigDecimal value, String reason) {
    var examSession =
        examSessionRepository
            .findById(examSessionId)
            .orElseThrow(() -> ResourceNotFoundException.of("Exam session", examSessionId));

    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));

    if (student.getRole() != UserRole.STUDENT) {
      throw new BadRequestException("This user is not a student");
    }

    if (!userRepository.existsById(enteredById)) {
      throw ResourceNotFoundException.of("Grade entry author", enteredById);
    }

    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(20)) > 0) {
      throw new BadRequestException("The grade must be between 0 and 20");
    }

    var isCorrection =
        gradeRepository.existsByExamSession_IdAndStudent_Id(examSessionId, studentId);

    if (isCorrection && (reason == null || reason.isBlank())) {
      throw new BadRequestException("A reason is required when correcting an existing grade");
    }

    var entity =
        JGrade.builder()
            .examSession(examSessionRepository.getReferenceById(examSessionId))
            .student(userRepository.getReferenceById(studentId))
            .value(value)
            .entryDate(LocalDateTime.now())
            .enteredBy(userRepository.getReferenceById(enteredById))
            .reason(reason)
            .build();

    return gradeMapper.toModel(gradeRepository.save(entity));
  }
}

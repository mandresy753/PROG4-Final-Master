package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

  @Mock private GradeRepository gradeRepository;
  @Mock private ExamSessionRepository examSessionRepository;
  @Mock private UserRepository userRepository;
  @Mock private GradeMapper gradeMapper;
  @InjectMocks private GradeService gradeService;

  private com.example.demo.entity.JUser buildStudent(UUID id) {
    return com.example.demo.entity.JUser.builder().id(id).role(UserRole.STUDENT).build();
  }

  private com.example.demo.entity.JExamSession buildExamSession(UUID id) {
    return com.example.demo.entity.JExamSession.builder().id(id).build();
  }

  @Test
  void history() {
    var examSessionId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var entity =
        com.example.demo.entity.JGrade.builder()
            .id(UUID.randomUUID())
            .value(new BigDecimal("15"))
            .entryDate(LocalDateTime.now())
            .build();
    when(gradeRepository.findByExamSession_IdAndStudent_IdOrderByEntryDateDesc(
            examSessionId, studentId))
        .thenReturn(List.of(entity));
    when(gradeMapper.toModel(entity))
        .thenReturn(
            Grade.builder()
                .id(entity.getId())
                .value(new BigDecimal("15"))
                .entryDate(entity.getEntryDate())
                .build());

    assertEquals(1, gradeService.history(examSessionId, studentId).size());
  }

  @Test
  void currentGradesForStudent() {
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var exam = com.example.demo.entity.JExam.builder().id(examId).build();
    var examSession =
        com.example.demo.entity.JExamSession.builder().id(UUID.randomUUID()).exam(exam).build();
    var entity =
        com.example.demo.entity.JGrade.builder()
            .id(UUID.randomUUID())
            .examSession(examSession)
            .value(new BigDecimal("15"))
            .entryDate(LocalDateTime.now())
            .build();
    when(gradeRepository.findByStudent_Id(studentId)).thenReturn(List.of(entity));
    when(gradeMapper.toModel(entity))
        .thenReturn(Grade.builder().id(entity.getId()).value(new BigDecimal("15")).build());

    assertEquals(1, gradeService.currentGradesForStudent(studentId).size());
  }

  @Test
  void record() {
    var examSessionId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var enteredById = UUID.randomUUID();
    var examSession = buildExamSession(examSessionId);
    var student = buildStudent(studentId);
    var author = com.example.demo.entity.JUser.builder().id(enteredById).build();

    when(examSessionRepository.findById(examSessionId)).thenReturn(Optional.of(examSession));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.existsById(enteredById)).thenReturn(true);

    var savedEntity =
        com.example.demo.entity.JGrade.builder()
            .id(UUID.randomUUID())
            .value(new BigDecimal("15"))
            .entryDate(LocalDateTime.now())
            .build();
    when(examSessionRepository.getReferenceById(examSessionId)).thenReturn(examSession);
    when(userRepository.getReferenceById(studentId)).thenReturn(student);
    when(userRepository.getReferenceById(enteredById)).thenReturn(author);
    when(gradeRepository.save(any())).thenReturn(savedEntity);
    when(gradeMapper.toModel(any()))
        .thenReturn(Grade.builder().id(savedEntity.getId()).value(new BigDecimal("15")).build());

    var result =
        gradeService.record(
            examSessionId, studentId, enteredById, new BigDecimal("15"), "Test reason");

    assertEquals(new BigDecimal("15"), result.value());
  }

  @Test
  void record_examSessionNotFound() {
    when(examSessionRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            gradeService.record(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, null));
  }

  @Test
  void record_studentNotFound() {
    when(examSessionRepository.findById(any()))
        .thenReturn(Optional.of(mock(com.example.demo.entity.JExamSession.class)));
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            gradeService.record(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, null));
  }

  @Test
  void record_notAStudent() {
    var teacherId = UUID.randomUUID();
    when(examSessionRepository.findById(any()))
        .thenReturn(Optional.of(mock(com.example.demo.entity.JExamSession.class)));
    when(userRepository.findById(teacherId))
        .thenReturn(
            Optional.of(
                com.example.demo.entity.JUser.builder()
                    .id(teacherId)
                    .role(UserRole.TEACHER)
                    .build()));
    assertThrows(
        BadRequestException.class,
        () ->
            gradeService.record(
                UUID.randomUUID(), teacherId, UUID.randomUUID(), BigDecimal.TEN, null));
  }

  @Test
  void record_gradeTooLow() {
    var studentId = UUID.randomUUID();
    when(examSessionRepository.findById(any()))
        .thenReturn(Optional.of(mock(com.example.demo.entity.JExamSession.class)));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(buildStudent(studentId)));
    when(userRepository.existsById(any())).thenReturn(true);
    assertThrows(
        BadRequestException.class,
        () ->
            gradeService.record(
                UUID.randomUUID(), studentId, UUID.randomUUID(), new BigDecimal("-1"), null));
  }

  @Test
  void record_gradeTooHigh() {
    var studentId = UUID.randomUUID();
    when(examSessionRepository.findById(any()))
        .thenReturn(Optional.of(mock(com.example.demo.entity.JExamSession.class)));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(buildStudent(studentId)));
    when(userRepository.existsById(any())).thenReturn(true);
    assertThrows(
        BadRequestException.class,
        () ->
            gradeService.record(
                UUID.randomUUID(), studentId, UUID.randomUUID(), new BigDecimal("21"), null));
  }

  @Test
  void record_firstEntry_reasonNotRequired() {
    var examSessionId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var enteredById = UUID.randomUUID();
    var examSession = buildExamSession(examSessionId);
    var student = buildStudent(studentId);
    var author = com.example.demo.entity.JUser.builder().id(enteredById).build();

    when(examSessionRepository.findById(examSessionId)).thenReturn(Optional.of(examSession));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.existsById(enteredById)).thenReturn(true);
    when(gradeRepository.existsByExamSession_IdAndStudent_Id(examSessionId, studentId))
        .thenReturn(false);

    var savedEntity =
        com.example.demo.entity.JGrade.builder()
            .id(UUID.randomUUID())
            .value(new BigDecimal("15"))
            .entryDate(LocalDateTime.now())
            .build();
    when(examSessionRepository.getReferenceById(examSessionId)).thenReturn(examSession);
    when(userRepository.getReferenceById(studentId)).thenReturn(student);
    when(userRepository.getReferenceById(enteredById)).thenReturn(author);
    when(gradeRepository.save(any())).thenReturn(savedEntity);
    when(gradeMapper.toModel(any()))
        .thenReturn(Grade.builder().id(savedEntity.getId()).value(new BigDecimal("15")).build());

    var result =
        gradeService.record(examSessionId, studentId, enteredById, new BigDecimal("15"), null);

    assertEquals(new BigDecimal("15"), result.value());
  }

  @Test
  void record_correction_reasonRequired() {
    var examSessionId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var enteredById = UUID.randomUUID();
    var examSession = buildExamSession(examSessionId);
    var student = buildStudent(studentId);

    when(examSessionRepository.findById(examSessionId)).thenReturn(Optional.of(examSession));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.existsById(enteredById)).thenReturn(true);
    when(gradeRepository.existsByExamSession_IdAndStudent_Id(examSessionId, studentId))
        .thenReturn(true);

    assertThrows(
        BadRequestException.class,
        () ->
            gradeService.record(examSessionId, studentId, enteredById, new BigDecimal("15"), null));
  }

  @Test
  void record_correction_blankReasonRejected() {
    var examSessionId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var enteredById = UUID.randomUUID();
    var examSession = buildExamSession(examSessionId);
    var student = buildStudent(studentId);

    when(examSessionRepository.findById(examSessionId)).thenReturn(Optional.of(examSession));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.existsById(enteredById)).thenReturn(true);
    when(gradeRepository.existsByExamSession_IdAndStudent_Id(examSessionId, studentId))
        .thenReturn(true);

    assertThrows(
        BadRequestException.class,
        () ->
            gradeService.record(
                examSessionId, studentId, enteredById, new BigDecimal("15"), "   "));
  }

  @Test
  void record_correction_succeedsWithReason() {
    var examSessionId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var enteredById = UUID.randomUUID();
    var examSession = buildExamSession(examSessionId);
    var student = buildStudent(studentId);
    var author = com.example.demo.entity.JUser.builder().id(enteredById).build();

    when(examSessionRepository.findById(examSessionId)).thenReturn(Optional.of(examSession));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.existsById(enteredById)).thenReturn(true);
    when(gradeRepository.existsByExamSession_IdAndStudent_Id(examSessionId, studentId))
        .thenReturn(true);

    var savedEntity =
        com.example.demo.entity.JGrade.builder()
            .id(UUID.randomUUID())
            .value(new BigDecimal("17"))
            .entryDate(LocalDateTime.now())
            .build();
    when(examSessionRepository.getReferenceById(examSessionId)).thenReturn(examSession);
    when(userRepository.getReferenceById(studentId)).thenReturn(student);
    when(userRepository.getReferenceById(enteredById)).thenReturn(author);
    when(gradeRepository.save(any())).thenReturn(savedEntity);
    when(gradeMapper.toModel(any()))
        .thenReturn(Grade.builder().id(savedEntity.getId()).value(new BigDecimal("17")).build());

    var result =
        gradeService.record(
            examSessionId, studentId, enteredById, new BigDecimal("17"), "Reclamation etudiant");

    assertEquals(new BigDecimal("17"), result.value());
  }

  @Test
  void record_authorNotFound() {
    var studentId = UUID.randomUUID();
    var enteredById = UUID.randomUUID();
    when(examSessionRepository.findById(any()))
        .thenReturn(Optional.of(mock(com.example.demo.entity.JExamSession.class)));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(buildStudent(studentId)));
    when(userRepository.existsById(enteredById)).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class,
        () -> gradeService.record(UUID.randomUUID(), studentId, enteredById, BigDecimal.TEN, null));
  }
}

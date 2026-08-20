package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.Track;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ExamSessionMapper;
import com.example.demo.model.ExamSession;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.ExamSessionRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamSessionServiceTest {

  @Mock private ExamSessionRepository examSessionRepository;
  @Mock private ExamRepository examRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private UserRepository userRepository;
  @Mock private ExamSessionMapper examSessionMapper;
  @InjectMocks private ExamSessionService examSessionService;

  private com.example.demo.entity.JGroup buildGroup(UUID id) {
    return com.example.demo.entity.JGroup.builder()
        .id(id)
        .reference("K1")
        .track(Track.TRONC_COMMUN)
        .build();
  }

  @Test
  void findByExam() {
    var examId = UUID.randomUUID();
    when(examRepository.existsById(examId)).thenReturn(true);
    var entity =
        com.example.demo.entity.JExamSession.builder()
            .id(UUID.randomUUID())
            .examDate(LocalDateTime.of(2025, 6, 1, 9, 0))
            .build();
    when(examSessionRepository.findByExam_Id(examId)).thenReturn(List.of(entity));
    when(examSessionMapper.toModel(entity))
        .thenReturn(
            ExamSession.builder().id(entity.getId()).examDate(entity.getExamDate()).build());

    assertEquals(1, examSessionService.findByExam(examId).size());
  }

  @Test
  void findByExam_notFound() {
    when(examRepository.existsById(any())).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class, () -> examSessionService.findByExam(UUID.randomUUID()));
  }

  @Test
  void create() {
    var examId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var group = buildGroup(groupId);
    var courseOffering =
        com.example.demo.entity.JCourseOffering.builder()
            .groups(new HashSet<>(Set.of(group)))
            .build();
    var exam =
        com.example.demo.entity.JExam.builder().id(examId).courseOffering(courseOffering).build();
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(groupRepository.findAllById(any())).thenReturn(List.of(group));
    when(examSessionRepository.findByExam_Id(examId)).thenReturn(List.of());
    var saved =
        com.example.demo.entity.JExamSession.builder()
            .id(UUID.randomUUID())
            .examDate(LocalDateTime.of(2025, 6, 1, 9, 0))
            .build();
    when(examSessionRepository.save(any())).thenReturn(saved);
    when(examSessionMapper.toModel(any()))
        .thenReturn(ExamSession.builder().id(saved.getId()).examDate(saved.getExamDate()).build());

    assertNotNull(
        examSessionService.create(
            examId, LocalDateTime.of(2025, 6, 1, 9, 0), null, List.of(groupId)));
  }

  @Test
  void create_emptyGroups() {
    when(examRepository.findById(any()))
        .thenReturn(Optional.of(mock(com.example.demo.entity.JExam.class)));
    assertThrows(
        BadRequestException.class,
        () -> examSessionService.create(UUID.randomUUID(), LocalDateTime.now(), null, List.of()));
  }

  @Test
  void create_nullGroups() {
    when(examRepository.findById(any()))
        .thenReturn(Optional.of(mock(com.example.demo.entity.JExam.class)));
    assertThrows(
        BadRequestException.class,
        () -> examSessionService.create(UUID.randomUUID(), LocalDateTime.now(), null, null));
  }

  @Test
  void create_examNotFound() {
    when(examRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            examSessionService.create(
                UUID.randomUUID(), LocalDateTime.now(), null, List.of(UUID.randomUUID())));
  }

  @Test
  void create_groupNotFound() {
    var examId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var exam =
        com.example.demo.entity.JExam.builder()
            .id(examId)
            .courseOffering(
                com.example.demo.entity.JCourseOffering.builder().groups(Set.of()).build())
            .build();
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(groupRepository.findAllById(any())).thenReturn(List.of());

    assertThrows(
        ResourceNotFoundException.class,
        () -> examSessionService.create(examId, LocalDateTime.now(), null, List.of(groupId)));
  }

  @Test
  void create_groupNotInOffering() {
    var examId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var otherGroupId = UUID.randomUUID();
    var group = buildGroup(groupId);
    var otherGroup = buildGroup(otherGroupId);
    var courseOffering =
        com.example.demo.entity.JCourseOffering.builder()
            .groups(new HashSet<>(Set.of(group)))
            .build();
    var exam =
        com.example.demo.entity.JExam.builder().id(examId).courseOffering(courseOffering).build();
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(groupRepository.findAllById(any())).thenReturn(List.of(otherGroup));

    assertThrows(
        BadRequestException.class,
        () -> examSessionService.create(examId, LocalDateTime.now(), null, List.of(otherGroupId)));
  }

  @Test
  void create_groupAlreadyHasSession() {
    var examId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var group = buildGroup(groupId);
    var courseOffering =
        com.example.demo.entity.JCourseOffering.builder()
            .groups(new HashSet<>(Set.of(group)))
            .build();
    var exam =
        com.example.demo.entity.JExam.builder().id(examId).courseOffering(courseOffering).build();
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(groupRepository.findAllById(any())).thenReturn(List.of(group));
    var existingSession =
        com.example.demo.entity.JExamSession.builder().groups(Set.of(group)).build();
    when(examSessionRepository.findByExam_Id(examId)).thenReturn(List.of(existingSession));

    assertThrows(
        BadRequestException.class,
        () -> examSessionService.create(examId, LocalDateTime.now(), null, List.of(groupId)));
  }

  @Test
  void delete() {
    var id = UUID.randomUUID();
    when(examSessionRepository.existsById(id)).thenReturn(true);
    examSessionService.delete(id);
    verify(examSessionRepository).deleteById(id);
  }

  @Test
  void delete_notFound() {
    var id = UUID.randomUUID();
    when(examSessionRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> examSessionService.delete(id));
  }
}

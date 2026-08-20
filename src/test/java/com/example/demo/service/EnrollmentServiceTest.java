package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.Level;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.EnrollmentMapper;
import com.example.demo.model.Enrollment;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

  @Mock private EnrollmentRepository enrollmentRepository;
  @Mock private UserRepository userRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private EnrollmentMapper enrollmentMapper;
  @InjectMocks private EnrollmentService enrollmentService;

  private com.example.demo.entity.JUser buildStudent(UUID id) {
    return com.example.demo.entity.JUser.builder()
        .id(id)
        .reference("STD0001")
        .lastName("Rakoto")
        .firstName("Jean")
        .email("jean@test.com")
        .password("hash")
        .role(UserRole.STUDENT)
        .build();
  }

  private com.example.demo.entity.JGroup buildGroup(UUID id, Track track) {
    return com.example.demo.entity.JGroup.builder().id(id).reference("K1").track(track).build();
  }

  private com.example.demo.entity.JAcademicYear buildYear(UUID id) {
    return com.example.demo.entity.JAcademicYear.builder()
        .id(id)
        .label("2025-2026")
        .startDate(LocalDate.of(2025, 9, 1))
        .endDate(LocalDate.of(2026, 7, 31))
        .build();
  }

  private Enrollment buildEnrollment(UUID id) {
    return Enrollment.builder().id(id).level(Level.L1).startDate(LocalDate.of(2025, 9, 1)).build();
  }

  @Test
  void findByStudent() {
    var studentId = UUID.randomUUID();
    var entity =
        com.example.demo.entity.JEnrollment.builder()
            .id(UUID.randomUUID())
            .student(buildStudent(studentId))
            .group(buildGroup(UUID.randomUUID(), Track.TRONC_COMMUN))
            .academicYear(buildYear(UUID.randomUUID()))
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.findByStudent_IdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(entity));
    when(enrollmentMapper.toModel(entity)).thenReturn(buildEnrollment(entity.getId()));

    assertEquals(1, enrollmentService.findByStudent(studentId).size());
  }

  @Test
  void findByGroupAndAcademicYear() {
    var groupId = UUID.randomUUID();
    var yearId = UUID.randomUUID();
    when(enrollmentRepository.findByGroup_IdAndAcademicYear_Id(groupId, yearId))
        .thenReturn(List.of());
    assertEquals(0, enrollmentService.findByGroupAndAcademicYear(groupId, yearId).size());
  }

  @Test
  void create() {
    var studentId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var yearId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(buildStudent(studentId)));
    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(academicYearRepository.existsById(yearId)).thenReturn(true);
    var entity =
        com.example.demo.entity.JEnrollment.builder()
            .id(UUID.randomUUID())
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.save(any())).thenReturn(entity);
    when(enrollmentMapper.toModel(any())).thenReturn(buildEnrollment(entity.getId()));

    assertNotNull(
        enrollmentService.create(
            studentId, groupId, yearId, Level.L1, LocalDate.of(2025, 9, 1), null));
  }

  @Test
  void create_notStudent() {
    var teacherId = UUID.randomUUID();
    var teacher =
        com.example.demo.entity.JUser.builder().id(teacherId).role(UserRole.TEACHER).build();
    when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

    assertThrows(
        BadRequestException.class,
        () ->
            enrollmentService.create(
                teacherId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                Level.L1,
                LocalDate.of(2025, 9, 1),
                null));
  }

  @Test
  void create_studentNotFound() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            enrollmentService.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Level.L1,
                LocalDate.of(2025, 9, 1),
                null));
  }

  @Test
  void create_groupNotFound() {
    var studentId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(buildStudent(studentId)));
    when(groupRepository.existsById(any())).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            enrollmentService.create(
                studentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                Level.L1,
                LocalDate.of(2025, 9, 1),
                null));
  }

  @Test
  void create_yearNotFound() {
    var studentId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(buildStudent(studentId)));
    when(groupRepository.existsById(groupId)).thenReturn(true);
    when(academicYearRepository.existsById(any())).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            enrollmentService.create(
                studentId, groupId, UUID.randomUUID(), Level.L1, LocalDate.of(2025, 9, 1), null));
  }

  @Test
  void delete() {
    var id = UUID.randomUUID();
    when(enrollmentRepository.existsById(id)).thenReturn(true);
    enrollmentService.delete(id);
    verify(enrollmentRepository).deleteById(id);
  }

  @Test
  void delete_notFound() {
    var id = UUID.randomUUID();
    when(enrollmentRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> enrollmentService.delete(id));
  }

  @Test
  void trackForYear() {
    var studentId = UUID.randomUUID();
    var yearId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var entity =
        com.example.demo.entity.JEnrollment.builder()
            .student(buildStudent(studentId))
            .group(buildGroup(groupId, Track.EL))
            .academicYear(buildYear(yearId))
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.findByStudent_Id(studentId)).thenReturn(List.of(entity));

    assertEquals(Track.EL, enrollmentService.trackForYear(studentId, yearId));
  }

  @Test
  void trackForYear_empty() {
    when(enrollmentRepository.findByStudent_Id(any())).thenReturn(List.of());
    assertThrows(
        BadRequestException.class,
        () -> enrollmentService.trackForYear(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void trackForYear_conflictingTracks() {
    var studentId = UUID.randomUUID();
    var yearId = UUID.randomUUID();
    var el =
        com.example.demo.entity.JEnrollment.builder()
            .student(buildStudent(studentId))
            .group(buildGroup(UUID.randomUUID(), Track.EL))
            .academicYear(buildYear(yearId))
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    var tn =
        com.example.demo.entity.JEnrollment.builder()
            .student(buildStudent(studentId))
            .group(buildGroup(UUID.randomUUID(), Track.TN))
            .academicYear(buildYear(yearId))
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.findByStudent_Id(studentId)).thenReturn(List.of(el, tn));

    assertThrows(ConflictException.class, () -> enrollmentService.trackForYear(studentId, yearId));
  }

  @Test
  void currentTrack() {
    var studentId = UUID.randomUUID();
    var entity =
        com.example.demo.entity.JEnrollment.builder()
            .student(buildStudent(studentId))
            .group(buildGroup(UUID.randomUUID(), Track.EL))
            .academicYear(buildYear(UUID.randomUUID()))
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.findByStudent_Id(studentId)).thenReturn(List.of(entity));

    assertEquals(Track.EL, enrollmentService.currentTrack(studentId));
  }

  @Test
  void currentTrack_empty() {
    when(enrollmentRepository.findByStudent_Id(any())).thenReturn(List.of());
    assertThrows(
        BadRequestException.class, () -> enrollmentService.currentTrack(UUID.randomUUID()));
  }

  @Test
  void finalTrack() {
    var studentId = UUID.randomUUID();
    var entity =
        com.example.demo.entity.JEnrollment.builder()
            .student(buildStudent(studentId))
            .group(buildGroup(UUID.randomUUID(), Track.EL))
            .academicYear(buildYear(UUID.randomUUID()))
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.findByStudent_Id(studentId)).thenReturn(List.of(entity));

    assertEquals(Track.EL, enrollmentService.finalTrack(studentId).orElse(null));
  }

  @Test
  void finalTrack_onlyTroncCommun() {
    var studentId = UUID.randomUUID();
    var entity =
        com.example.demo.entity.JEnrollment.builder()
            .student(buildStudent(studentId))
            .group(buildGroup(UUID.randomUUID(), Track.TRONC_COMMUN))
            .academicYear(buildYear(UUID.randomUUID()))
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.findByStudent_Id(studentId)).thenReturn(List.of(entity));

    assertTrue(enrollmentService.finalTrack(studentId).isEmpty());
  }
}

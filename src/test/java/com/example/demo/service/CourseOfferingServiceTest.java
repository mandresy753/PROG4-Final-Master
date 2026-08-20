package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseOfferingMapper;
import com.example.demo.model.CourseOffering;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.GroupRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseOfferingServiceTest {

  @Mock private CourseOfferingRepository courseOfferingRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private CourseOfferingMapper courseOfferingMapper;
  @Mock private SemesterCreditPolicy semesterCreditPolicy;
  @InjectMocks private CourseOfferingService courseOfferingService;

  private com.example.demo.entity.JCourse buildCourse(UUID id) {
    return com.example.demo.entity.JCourse.builder()
        .id(id)
        .ref("PROG1")
        .title("Programmation 1")
        .creditCount(6)
        .track(Track.TRONC_COMMUN)
        .semester(Semester.S1)
        .build();
  }

  private com.example.demo.entity.JGroup buildGroup(UUID id) {
    return com.example.demo.entity.JGroup.builder()
        .id(id)
        .reference("K1")
        .track(Track.TRONC_COMMUN)
        .build();
  }

  @Test
  void findAll() {
    when(courseOfferingRepository.findAll()).thenReturn(List.of());
    assertEquals(0, courseOfferingService.findAll().size());
  }

  @Test
  void findByAcademicYear() {
    var yearId = UUID.randomUUID();
    when(courseOfferingRepository.findByAcademicYear_Id(yearId)).thenReturn(List.of());
    assertEquals(0, courseOfferingService.findByAcademicYear(yearId).size());
  }

  @Test
  void findByGroupAndAcademicYear() {
    when(courseOfferingRepository.findByGroupIdAndAcademicYear_Id(any(), any()))
        .thenReturn(List.of());
    assertEquals(
        0,
        courseOfferingService
            .findByGroupAndAcademicYear(UUID.randomUUID(), UUID.randomUUID())
            .size());
  }

  @Test
  void findById_found() {
    var id = UUID.randomUUID();
    var entity = com.example.demo.entity.JCourseOffering.builder().id(id).build();
    when(courseOfferingRepository.findById(id)).thenReturn(Optional.of(entity));
    when(courseOfferingMapper.toModel(entity)).thenReturn(CourseOffering.builder().id(id).build());

    assertNotNull(courseOfferingService.findById(id));
  }

  @Test
  void findById_notFound() {
    when(courseOfferingRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> courseOfferingService.findById(UUID.randomUUID()));
  }

  @Test
  void create() {
    var courseId = UUID.randomUUID();
    var yearId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var course = buildCourse(courseId);
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(academicYearRepository.existsById(yearId)).thenReturn(true);
    when(groupRepository.findAllById(any())).thenReturn(List.of(buildGroup(groupId)));
    when(courseOfferingRepository.findByCourse_IdAndAcademicYear_IdAndGroupId(any(), any(), any()))
        .thenReturn(List.of());
    when(courseOfferingRepository.findByGroupIdAndAcademicYear_Id(any(), any()))
        .thenReturn(List.of());
    var entity =
        com.example.demo.entity.JCourseOffering.builder()
            .id(UUID.randomUUID())
            .course(course)
            .build();
    when(courseOfferingRepository.save(any())).thenReturn(entity);
    when(courseOfferingMapper.toModel(any()))
        .thenReturn(CourseOffering.builder().id(entity.getId()).build());

    assertNotNull(courseOfferingService.create(courseId, yearId, List.of(groupId)));
  }

  @Test
  void create_courseNotFound() {
    when(courseRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            courseOfferingService.create(
                UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID())));
  }

  @Test
  void create_yearNotFound() {
    when(courseRepository.findById(any())).thenReturn(Optional.of(buildCourse(UUID.randomUUID())));
    when(academicYearRepository.existsById(any())).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            courseOfferingService.create(
                UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID())));
  }

  @Test
  void create_emptyGroups() {
    when(courseRepository.findById(any())).thenReturn(Optional.of(buildCourse(UUID.randomUUID())));
    when(academicYearRepository.existsById(any())).thenReturn(true);
    assertThrows(
        BadRequestException.class,
        () -> courseOfferingService.create(UUID.randomUUID(), UUID.randomUUID(), List.of()));
  }

  @Test
  void create_nullGroups() {
    when(courseRepository.findById(any())).thenReturn(Optional.of(buildCourse(UUID.randomUUID())));
    when(academicYearRepository.existsById(any())).thenReturn(true);
    assertThrows(
        BadRequestException.class,
        () -> courseOfferingService.create(UUID.randomUUID(), UUID.randomUUID(), null));
  }

  @Test
  void create_groupNotFound() {
    when(courseRepository.findById(any())).thenReturn(Optional.of(buildCourse(UUID.randomUUID())));
    when(academicYearRepository.existsById(any())).thenReturn(true);
    when(groupRepository.findAllById(any())).thenReturn(List.of());
    assertThrows(
        ResourceNotFoundException.class,
        () ->
            courseOfferingService.create(
                UUID.randomUUID(), UUID.randomUUID(), List.of(UUID.randomUUID())));
  }

  @Test
  void create_alreadyAssigned() {
    var courseId = UUID.randomUUID();
    var yearId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(buildCourse(courseId)));
    when(academicYearRepository.existsById(yearId)).thenReturn(true);
    when(groupRepository.findAllById(any())).thenReturn(List.of(buildGroup(groupId)));
    when(courseOfferingRepository.findByCourse_IdAndAcademicYear_IdAndGroupId(
            courseId, yearId, groupId))
        .thenReturn(List.of(mock(com.example.demo.entity.JCourseOffering.class)));

    assertThrows(
        ConflictException.class,
        () -> courseOfferingService.create(courseId, yearId, List.of(groupId)));
  }

  @Test
  void delete() {
    var id = UUID.randomUUID();
    when(courseOfferingRepository.existsById(id)).thenReturn(true);
    courseOfferingService.delete(id);
    verify(courseOfferingRepository).deleteById(id);
  }

  @Test
  void delete_notFound() {
    var id = UUID.randomUUID();
    when(courseOfferingRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> courseOfferingService.delete(id));
  }
}

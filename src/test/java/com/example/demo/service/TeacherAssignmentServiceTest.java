package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.UserRole;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.TeacherAssignmentMapper;
import com.example.demo.model.TeacherAssignment;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeacherAssignmentServiceTest {

  @Mock private TeacherAssignmentRepository teacherAssignmentRepository;
  @Mock private CourseOfferingRepository courseOfferingRepository;
  @Mock private UserRepository userRepository;
  @Mock private TeacherAssignmentMapper teacherAssignmentMapper;
  @InjectMocks private TeacherAssignmentService teacherAssignmentService;

  @Test
  void findByTeacher() {
    var teacherId = UUID.randomUUID();
    var entity = com.example.demo.entity.JTeacherAssignment.builder().id(UUID.randomUUID()).build();
    when(teacherAssignmentRepository.findByTeacher_Id(teacherId)).thenReturn(List.of(entity));
    when(teacherAssignmentMapper.toModel(entity))
        .thenReturn(TeacherAssignment.builder().id(entity.getId()).build());

    assertEquals(1, teacherAssignmentService.findByTeacher(teacherId).size());
  }

  @Test
  void findByCourseOffering() {
    var offeringId = UUID.randomUUID();
    when(teacherAssignmentRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of());

    assertEquals(0, teacherAssignmentService.findByCourseOffering(offeringId).size());
  }

  @Test
  void assign() {
    var offeringId = UUID.randomUUID();
    var teacherId = UUID.randomUUID();
    when(courseOfferingRepository.existsById(offeringId)).thenReturn(true);
    when(userRepository.findById(teacherId))
        .thenReturn(
            Optional.of(
                com.example.demo.entity.JUser.builder()
                    .id(teacherId)
                    .role(UserRole.TEACHER)
                    .build()));
    var entity = com.example.demo.entity.JTeacherAssignment.builder().id(UUID.randomUUID()).build();
    when(teacherAssignmentRepository.save(any())).thenReturn(entity);
    when(teacherAssignmentMapper.toModel(any()))
        .thenReturn(TeacherAssignment.builder().id(entity.getId()).build());

    assertNotNull(teacherAssignmentService.assign(offeringId, teacherId));
  }

  @Test
  void assign_offeringNotFound() {
    when(courseOfferingRepository.existsById(any())).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class,
        () -> teacherAssignmentService.assign(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void assign_teacherNotFound() {
    when(courseOfferingRepository.existsById(any())).thenReturn(true);
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> teacherAssignmentService.assign(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void assign_notATeacher() {
    when(courseOfferingRepository.existsById(any())).thenReturn(true);
    when(userRepository.findById(any()))
        .thenReturn(
            Optional.of(com.example.demo.entity.JUser.builder().role(UserRole.STUDENT).build()));
    assertThrows(
        BadRequestException.class,
        () -> teacherAssignmentService.assign(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void unassign() {
    var id = UUID.randomUUID();
    when(teacherAssignmentRepository.existsById(id)).thenReturn(true);
    teacherAssignmentService.unassign(id);
    verify(teacherAssignmentRepository).deleteById(id);
  }

  @Test
  void unassign_notFound() {
    var id = UUID.randomUUID();
    when(teacherAssignmentRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> teacherAssignmentService.unassign(id));
  }
}

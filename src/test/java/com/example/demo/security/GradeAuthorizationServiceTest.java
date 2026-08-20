package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.UserRole;
import com.example.demo.repository.ExamSessionRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class GradeAuthorizationServiceTest {

  @Mock private ExamSessionRepository examSessionRepository;
  @Mock private TeacherAssignmentRepository teacherAssignmentRepository;
  @InjectMocks private GradeAuthorizationService gradeAuthorizationService;

  @Test
  void canGrade_admin() {
    var auth = mock(Authentication.class);
    var principal =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(UUID.randomUUID())
                .email("admin@test.com")
                .password("hash")
                .role(UserRole.ADMIN)
                .build());
    when(auth.getPrincipal()).thenReturn(principal);

    assertTrue(gradeAuthorizationService.canGrade(UUID.randomUUID(), auth));
  }

  @Test
  void canGrade_student() {
    var auth = mock(Authentication.class);
    var principal =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(UUID.randomUUID())
                .email("student@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());
    when(auth.getPrincipal()).thenReturn(principal);

    assertFalse(gradeAuthorizationService.canGrade(UUID.randomUUID(), auth));
  }

  @Test
  void canGrade_teacher_assigned() {
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    var examSessionId = UUID.randomUUID();
    var auth = mock(Authentication.class);
    var principal =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(teacherId)
                .email("teacher@test.com")
                .password("hash")
                .role(UserRole.TEACHER)
                .build());
    when(auth.getPrincipal()).thenReturn(principal);

    var courseOffering = com.example.demo.entity.JCourseOffering.builder().id(offeringId).build();
    var exam =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .courseOffering(courseOffering)
            .build();
    var examSession =
        com.example.demo.entity.JExamSession.builder().id(examSessionId).exam(exam).build();
    when(examSessionRepository.findById(examSessionId)).thenReturn(Optional.of(examSession));

    var assignment =
        com.example.demo.entity.JTeacherAssignment.builder()
            .teacher(com.example.demo.entity.JUser.builder().id(teacherId).build())
            .courseOffering(courseOffering)
            .build();
    when(teacherAssignmentRepository.findByCourseOffering_Id(offeringId))
        .thenReturn(List.of(assignment));

    assertTrue(gradeAuthorizationService.canGrade(examSessionId, auth));
  }

  @Test
  void canGrade_teacher_notAssigned() {
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    var examSessionId = UUID.randomUUID();
    var auth = mock(Authentication.class);
    var principal =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(teacherId)
                .email("teacher@test.com")
                .password("hash")
                .role(UserRole.TEACHER)
                .build());
    when(auth.getPrincipal()).thenReturn(principal);

    var courseOffering = com.example.demo.entity.JCourseOffering.builder().id(offeringId).build();
    var exam =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .courseOffering(courseOffering)
            .build();
    var examSession =
        com.example.demo.entity.JExamSession.builder().id(examSessionId).exam(exam).build();
    when(examSessionRepository.findById(examSessionId)).thenReturn(Optional.of(examSession));
    when(teacherAssignmentRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of());

    assertFalse(gradeAuthorizationService.canGrade(examSessionId, auth));
  }
}

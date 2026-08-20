package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.UserRole;
import com.example.demo.repository.ExamSessionRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ExtendWith(MockitoExtension.class)
class GradeReadAuthorizationManagerTest {

  @Mock private ExamSessionRepository examSessionRepository;
  @Mock private TeacherAssignmentRepository teacherAssignmentRepository;
  @InjectMocks private GradeReadAuthorizationManager gradeReadAuthorizationManager;

  private RequestAuthorizationContext createContext(String... params) {
    var request = new MockHttpServletRequest();
    for (int i = 0; i < params.length; i += 2) {
      request.setParameter(params[i], params[i + 1]);
    }
    return new RequestAuthorizationContext(request, java.util.Map.of());
  }

  @Test
  void check_admin() {
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

    var decision =
        gradeReadAuthorizationManager.check(
            () -> auth, createContext("studentId", UUID.randomUUID().toString()));
    assertTrue(decision.isGranted());
  }

  @Test
  void check_student_ownData() {
    var studentId = UUID.randomUUID();
    var auth = mock(Authentication.class);
    var principal =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(studentId)
                .email("student@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());
    when(auth.getPrincipal()).thenReturn(principal);

    var decision =
        gradeReadAuthorizationManager.check(
            () -> auth, createContext("studentId", studentId.toString()));
    assertTrue(decision.isGranted());
  }

  @Test
  void check_student_otherData() {
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

    var decision =
        gradeReadAuthorizationManager.check(
            () -> auth, createContext("studentId", UUID.randomUUID().toString()));
    assertFalse(decision.isGranted());
  }

  @Test
  void check_student_noParam() {
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

    var decision = gradeReadAuthorizationManager.check(() -> auth, createContext());
    assertFalse(decision.isGranted());
  }

  @Test
  void check_teacher_withCourseOffering() {
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
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

    var assignment =
        com.example.demo.entity.JTeacherAssignment.builder()
            .teacher(com.example.demo.entity.JUser.builder().id(teacherId).build())
            .build();
    when(teacherAssignmentRepository.findByCourseOffering_Id(offeringId))
        .thenReturn(List.of(assignment));

    var decision =
        gradeReadAuthorizationManager.check(
            () -> auth,
            createContext(
                "studentId",
                UUID.randomUUID().toString(),
                "courseOfferingId",
                offeringId.toString()));
    assertTrue(decision.isGranted());
  }

  @Test
  void check_teacher_notAssignedToCourseOffering() {
    var teacherId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
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
    when(teacherAssignmentRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of());

    var decision =
        gradeReadAuthorizationManager.check(
            () -> auth,
            createContext(
                "studentId",
                UUID.randomUUID().toString(),
                "courseOfferingId",
                offeringId.toString()));
    assertFalse(decision.isGranted());
  }

  @Test
  void check_teacher_noCourseOffering() {
    var auth = mock(Authentication.class);
    var principal =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(UUID.randomUUID())
                .email("teacher@test.com")
                .password("hash")
                .role(UserRole.TEACHER)
                .build());
    when(auth.getPrincipal()).thenReturn(principal);

    var decision =
        gradeReadAuthorizationManager.check(
            () -> auth, createContext("studentId", UUID.randomUUID().toString()));
    assertFalse(decision.isGranted());
  }

  @Test
  void check_nonPrincipal() {
    var auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn("not-a-principal");

    var decision = gradeReadAuthorizationManager.check(() -> auth, createContext());
    assertFalse(decision.isGranted());
  }
}

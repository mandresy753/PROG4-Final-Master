package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.UserRole;
import com.example.demo.security.authorization.SelfOrAdminAuthorizationManager;
import com.example.demo.security.authorization.SelfOrStaffAuthorizationManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@ExtendWith(MockitoExtension.class)
class AuthorizationManagersTest {

  @InjectMocks private SelfOrAdminAuthorizationManager selfOrAdminManager;
  @InjectMocks private SelfOrStaffAuthorizationManager selfOrStaffManager;

  private Authentication mockAuth(UserRole role, UUID id) {
    var auth = mock(Authentication.class);
    var principal =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(id)
                .email("test@test.com")
                .password("hash")
                .role(role)
                .build());
    when(auth.getPrincipal()).thenReturn(principal);
    return auth;
  }

  @Test
  void selfOrAdmin_admin_granted() {
    var auth = mockAuth(UserRole.ADMIN, UUID.randomUUID());
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertTrue(selfOrAdminManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrAdmin_student_ownData() {
    var id = UUID.randomUUID();
    var auth = mockAuth(UserRole.STUDENT, id);
    var request = new MockHttpServletRequest();
    var context =
        new RequestAuthorizationContext(request, java.util.Map.of("studentId", id.toString()));
    assertTrue(selfOrAdminManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrAdmin_student_otherData() {
    var auth = mockAuth(UserRole.STUDENT, UUID.randomUUID());
    var request = new MockHttpServletRequest();
    var context =
        new RequestAuthorizationContext(
            request, java.util.Map.of("studentId", UUID.randomUUID().toString()));
    assertFalse(selfOrAdminManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrAdmin_student_noParam() {
    var auth = mockAuth(UserRole.STUDENT, UUID.randomUUID());
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertFalse(selfOrAdminManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrAdmin_teacher_denied() {
    var auth = mockAuth(UserRole.TEACHER, UUID.randomUUID());
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertFalse(selfOrAdminManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrAdmin_nonPrincipal() {
    var auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn("not-a-principal");
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertFalse(selfOrAdminManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrStaff_teacher_granted() {
    var auth = mockAuth(UserRole.TEACHER, UUID.randomUUID());
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertTrue(selfOrStaffManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrStaff_admin_granted() {
    var auth = mockAuth(UserRole.ADMIN, UUID.randomUUID());
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertTrue(selfOrStaffManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrStaff_student_ownData() {
    var id = UUID.randomUUID();
    var auth = mockAuth(UserRole.STUDENT, id);
    var request = new MockHttpServletRequest();
    request.setParameter("studentId", id.toString());
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertTrue(selfOrStaffManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrStaff_student_otherData() {
    var auth = mockAuth(UserRole.STUDENT, UUID.randomUUID());
    var request = new MockHttpServletRequest();
    request.setParameter("studentId", UUID.randomUUID().toString());
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertFalse(selfOrStaffManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrStaff_student_noParam() {
    var auth = mockAuth(UserRole.STUDENT, UUID.randomUUID());
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertFalse(selfOrStaffManager.check(() -> auth, context).isGranted());
  }

  @Test
  void selfOrStaff_nonPrincipal() {
    var auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn("not-a-principal");
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());
    assertFalse(selfOrStaffManager.check(() -> auth, context).isGranted());
  }
}

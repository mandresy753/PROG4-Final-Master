package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
class GradeAuthorizationManagerTest {

  @Mock private GradeAuthorizationService gradeAuthorizationService;
  @InjectMocks private GradeAuthorizationManager gradeAuthorizationManager;

  @Test
  void check_allowed() {
    var examSessionId = UUID.randomUUID();
    var auth = mock(Authentication.class);
    when(gradeAuthorizationService.canGrade(examSessionId, auth)).thenReturn(true);

    var request = new MockHttpServletRequest();
    var context =
        new RequestAuthorizationContext(
            request, java.util.Map.of("examSessionId", examSessionId.toString()));

    assertTrue(gradeAuthorizationManager.check(() -> auth, context).isGranted());
  }

  @Test
  void check_denied() {
    var examSessionId = UUID.randomUUID();
    var auth = mock(Authentication.class);
    when(gradeAuthorizationService.canGrade(examSessionId, auth)).thenReturn(false);

    var request = new MockHttpServletRequest();
    var context =
        new RequestAuthorizationContext(
            request, java.util.Map.of("examSessionId", examSessionId.toString()));

    assertFalse(gradeAuthorizationManager.check(() -> auth, context).isGranted());
  }

  @Test
  void check_nullExamSessionId() {
    var auth = mock(Authentication.class);
    var request = new MockHttpServletRequest();
    var context = new RequestAuthorizationContext(request, java.util.Map.of());

    assertFalse(gradeAuthorizationManager.check(() -> auth, context).isGranted());
  }

  @Test
  void check_invalidExamSessionId() {
    var auth = mock(Authentication.class);
    var request = new MockHttpServletRequest();
    var context =
        new RequestAuthorizationContext(request, java.util.Map.of("examSessionId", "not-a-uuid"));

    assertFalse(gradeAuthorizationManager.check(() -> auth, context).isGranted());
  }
}

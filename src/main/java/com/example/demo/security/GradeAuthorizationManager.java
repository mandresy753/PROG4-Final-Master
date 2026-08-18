package com.example.demo.security;

import java.util.UUID;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GradeAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private final GradeAuthorizationService gradeAuthorizationService;

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authentication, RequestAuthorizationContext context) {

    String examSessionId = context.getVariables().get("examSessionId");

    if (examSessionId == null) {
      return new AuthorizationDecision(false);
    }

    try {
      UUID uuid = UUID.fromString(examSessionId);

      boolean allowed = gradeAuthorizationService.canGrade(uuid, authentication.get());

      return new AuthorizationDecision(allowed);
    } catch (IllegalArgumentException e) {
      return new AuthorizationDecision(false);
    }
  }
}

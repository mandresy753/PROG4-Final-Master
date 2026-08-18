package com.example.demo.security.authorization;

import com.example.demo.enums.UserRole;
import com.example.demo.security.AppUserPrincipal;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class SelfOrAdminAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private static final String STUDENT_ID_KEY = "studentId";

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
    var authentication = authenticationSupplier.get();
    if (!(authentication.getPrincipal() instanceof AppUserPrincipal me)) {
      return new AuthorizationDecision(false);
    }

    if (me.getRole() == UserRole.ADMIN) {
      return new AuthorizationDecision(true);
    }

    if (me.getRole() != UserRole.STUDENT) {
      return new AuthorizationDecision(false);
    }

    var studentId = resolveStudentId(context);
    var granted = studentId != null && studentId.equals(me.getId().toString());
    return new AuthorizationDecision(granted);
  }

  private String resolveStudentId(RequestAuthorizationContext context) {
    var fromPath = context.getVariables().get(STUDENT_ID_KEY);
    if (fromPath != null) {
      return fromPath;
    }
    return context.getRequest().getParameter(STUDENT_ID_KEY);
  }
}

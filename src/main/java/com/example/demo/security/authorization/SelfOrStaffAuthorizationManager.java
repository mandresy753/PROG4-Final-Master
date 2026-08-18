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
public class SelfOrStaffAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
    var authentication = authenticationSupplier.get();
    if (!(authentication.getPrincipal() instanceof AppUserPrincipal me)) {
      return new AuthorizationDecision(false);
    }

    if (me.getRole() != UserRole.STUDENT) {
      return new AuthorizationDecision(true);
    }

    var studentId = context.getRequest().getParameter("studentId");
    var granted = studentId != null && studentId.equals(me.getId().toString());
    return new AuthorizationDecision(granted);
  }
}

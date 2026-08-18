package com.example.demo.security;

import com.example.demo.enums.UserRole;
import com.example.demo.repository.ExamSessionRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class GradeReadAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private final ExamSessionRepository examSessionRepository;
  private final TeacherAssignmentRepository teacherAssignmentRepository;

  public GradeReadAuthorizationManager(
      ExamSessionRepository examSessionRepository,
      TeacherAssignmentRepository teacherAssignmentRepository) {
    this.examSessionRepository = examSessionRepository;
    this.teacherAssignmentRepository = teacherAssignmentRepository;
  }

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authenticationSupplier, RequestAuthorizationContext context) {
    Authentication authentication = authenticationSupplier.get();

    if (!(authentication.getPrincipal() instanceof AppUserPrincipal me)) {
      return new AuthorizationDecision(false);
    }

    if (me.getRole() == UserRole.ADMIN) {
      return new AuthorizationDecision(true);
    }

    String studentIdParam = context.getRequest().getParameter("studentId");
    if (studentIdParam == null) {
      return new AuthorizationDecision(false);
    }

    if (me.getRole() == UserRole.STUDENT) {
      return new AuthorizationDecision(studentIdParam.equals(me.getId().toString()));
    }

    if (me.getRole() != UserRole.TEACHER) {
      return new AuthorizationDecision(false);
    }

    String examSessionIdParam = context.getRequest().getParameter("examSessionId");
    if (examSessionIdParam != null) {
      return canTeacherAccessExamSession(me.getId(), examSessionIdParam);
    }

    String courseOfferingIdParam = context.getRequest().getParameter("courseOfferingId");
    if (courseOfferingIdParam == null) {
      return new AuthorizationDecision(false);
    }

    return canTeacherAccessCourseOffering(me.getId(), courseOfferingIdParam);
  }

  private AuthorizationDecision canTeacherAccessExamSession(
      UUID teacherId, String examSessionIdParam) {
    try {
      UUID examSessionId = UUID.fromString(examSessionIdParam);
      return new AuthorizationDecision(
          examSessionRepository
              .findById(examSessionId)
              .map(
                  session ->
                      canTeacherAccessCourseOfferingValue(
                          teacherId, session.getExam().getCourseOffering().getId()))
              .orElse(false));
    } catch (IllegalArgumentException e) {
      return new AuthorizationDecision(false);
    }
  }

  private AuthorizationDecision canTeacherAccessCourseOffering(
      UUID teacherId, String courseOfferingIdParam) {
    try {
      UUID courseOfferingId = UUID.fromString(courseOfferingIdParam);
      return new AuthorizationDecision(
          canTeacherAccessCourseOfferingValue(teacherId, courseOfferingId));
    } catch (IllegalArgumentException e) {
      return new AuthorizationDecision(false);
    }
  }

  private boolean canTeacherAccessCourseOfferingValue(UUID teacherId, UUID courseOfferingId) {
    return teacherAssignmentRepository.findByCourseOffering_Id(courseOfferingId).stream()
        .anyMatch(assignment -> assignment.getTeacher().getId().equals(teacherId));
  }
}

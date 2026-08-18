package com.example.demo.security;

import com.example.demo.enums.UserRole;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ExamSessionRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GradeAuthorizationService {

  private final ExamSessionRepository examSessionRepository;
  private final TeacherAssignmentRepository teacherAssignmentRepository;

  public boolean canGrade(UUID examSessionId, Authentication authentication) {
    var me = (AppUserPrincipal) authentication.getPrincipal();

    if (me.getRole() == UserRole.ADMIN) {
      return true;
    }

    if (me.getRole() != UserRole.TEACHER) {
      return false;
    }

    var examSession =
        examSessionRepository
            .findById(examSessionId)
            .orElseThrow(() -> NotFoundException.of("Exam session", examSessionId));

    return teacherAssignmentRepository
        .findByCourseOffering_Id(examSession.getExam().getCourseOffering().getId())
        .stream()
        .anyMatch(teacherAssignment -> teacherAssignment.getTeacher().getId().equals(me.getId()));
  }
}

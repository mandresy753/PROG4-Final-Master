package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.CreateExamSessionRequest;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.ExamSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ExamSessionControllerIT extends ControllerIT {

  @Test
  void create_asAdmin_withGroupOfTheOffering_isAccepted() {
    var admin = createUser(UserRole.ADMIN);
    var teacher = createUser(UserRole.TEACHER);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));

    var request =
        new CreateExamSessionRequest(
            exam.getId(),
            LocalDateTime.now().plusDays(10),
            teacher.getId(),
            List.of(group.getId()));

    ResponseEntity<ExamSession> response =
        post("/exam-sessions", request, authHeaders(admin), ExamSession.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().groups()).extracting(g -> g.id()).containsExactly(group.getId());
  }

  @Test
  void create_withGroupNotInTheOffering_returns400() {

    var admin = createUser(UserRole.ADMIN);
    var course = createCourse(Track.TRONC_COMMUN, Semester.S3, 4);
    var year = createAcademicYear();
    var attachedGroup = createGroup(Track.EL);
    var strangerGroup = createGroup(Track.TN);
    var offering = createCourseOffering(course, year, attachedGroup);
    var exam = createExam(offering, new BigDecimal("1.00"));

    var request =
        new CreateExamSessionRequest(
            exam.getId(), LocalDateTime.now().plusDays(5), null, List.of(strangerGroup.getId()));

    ResponseEntity<String> response = post("/exam-sessions", request, authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void create_sameGroupTwiceForSameExam_returns400() {
    var admin = createUser(UserRole.ADMIN);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));

    var firstRequest =
        new CreateExamSessionRequest(
            exam.getId(), LocalDateTime.now().plusDays(2), null, List.of(group.getId()));
    post("/exam-sessions", firstRequest, authHeaders(admin), ExamSession.class);

    var secondRequest =
        new CreateExamSessionRequest(
            exam.getId(), LocalDateTime.now().plusDays(3), null, List.of(group.getId()));
    ResponseEntity<String> response = post("/exam-sessions", secondRequest, authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}

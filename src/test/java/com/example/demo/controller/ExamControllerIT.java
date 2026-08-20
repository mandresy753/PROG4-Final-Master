package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.CreateExamRequest;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Exam;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ExamControllerIT extends ControllerIT {

  @Test
  void create_asAdmin_persistsExamWithCoefficient() {
    var admin = createUser(UserRole.ADMIN);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var offering = createCourseOffering(course, year, createGroup(Track.EL));

    var request = new CreateExamRequest(offering.getId(), new BigDecimal("0.40"));

    ResponseEntity<Exam> response = post("/exams", request, authHeaders(admin), Exam.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().coefficient()).isEqualByComparingTo("0.40");
  }

  @Test
  void create_whenCoefficientSumExceedsOne_returns400() {

    var admin = createUser(UserRole.ADMIN);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var offering = createCourseOffering(course, year, createGroup(Track.EL));

    post(
        "/exams",
        new CreateExamRequest(offering.getId(), new BigDecimal("0.60")),
        authHeaders(admin),
        Exam.class);

    ResponseEntity<String> response =
        post(
            "/exams",
            new CreateExamRequest(offering.getId(), new BigDecimal("0.50")),
            authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void create_withExactSumOfOne_isAccepted() {
    var admin = createUser(UserRole.ADMIN);
    var course = createCourse(Track.TN, Semester.S5, 3);
    var year = createAcademicYear();
    var offering = createCourseOffering(course, year, createGroup(Track.TN));

    post(
        "/exams",
        new CreateExamRequest(offering.getId(), new BigDecimal("0.40")),
        authHeaders(admin),
        Exam.class);
    ResponseEntity<Exam> response =
        post(
            "/exams",
            new CreateExamRequest(offering.getId(), new BigDecimal("0.60")),
            authHeaders(admin),
            Exam.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void create_asStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S3, 4);
    var year = createAcademicYear();
    var offering = createCourseOffering(course, year, createGroup(Track.EL));

    ResponseEntity<String> response =
        post(
            "/exams",
            new CreateExamRequest(offering.getId(), new BigDecimal("0.30")),
            authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

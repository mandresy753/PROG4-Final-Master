package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.RecordGradeRequest;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Grade;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GradeControllerIT extends ControllerIT {

  @Test
  void record_asAssignedTeacher_isAccepted() {
    var admin = createUser(UserRole.ADMIN);
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));
    var session = createExamSession(exam, teacher, group);
    assignTeacher(offering, teacher);

    var request = new RecordGradeRequest(student.getId(), new BigDecimal("14.50"), "Note initiale");

    ResponseEntity<Grade> response =
        post("/grades/" + session.getId(), request, authHeaders(teacher), Grade.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().value()).isEqualByComparingTo("14.50");
    assertThat(response.getBody().enteredBy().id()).isEqualTo(teacher.getId());
  }

  @Test
  void record_asTeacherNotAssignedToThisCourse_isForbidden() {

    var teacher = createUser(UserRole.TEACHER);
    var otherTeacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));
    var session = createExamSession(exam, otherTeacher, group);
    assignTeacher(offering, otherTeacher);

    var request = new RecordGradeRequest(student.getId(), new BigDecimal("10.00"), "Tentative");

    ResponseEntity<String> response =
        post("/grades/" + session.getId(), request, authHeaders(teacher));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void record_asStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var teacher = createUser(UserRole.TEACHER);
    var course = createCourse(Track.TN, Semester.S5, 3);
    var year = createAcademicYear();
    var group = createGroup(Track.TN);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));
    var session = createExamSession(exam, teacher, group);
    assignTeacher(offering, teacher);

    var request = new RecordGradeRequest(student.getId(), new BigDecimal("12.00"), "x");

    ResponseEntity<String> response =
        post("/grades/" + session.getId(), request, authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void record_asAdmin_isAlwaysAllowed() {
    var admin = createUser(UserRole.ADMIN);
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S3, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));
    var session = createExamSession(exam, teacher, group);

    var request =
        new RecordGradeRequest(student.getId(), new BigDecimal("16.00"), "Admin override");

    ResponseEntity<Grade> response =
        post("/grades/" + session.getId(), request, authHeaders(admin), Grade.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void record_withValueAboveTwenty_returns400() {
    var admin = createUser(UserRole.ADMIN);
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S3, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));
    var session = createExamSession(exam, teacher, group);

    var request =
        new RecordGradeRequest(student.getId(), new BigDecimal("25.00"), "Erreur de saisie");

    ResponseEntity<String> response =
        post("/grades/" + session.getId(), request, authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void gradeCanBeCorrected_andHistoryKeepsBothEntriesWithReason() {

    var admin = createUser(UserRole.ADMIN);
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));
    var session = createExamSession(exam, teacher, group);
    assignTeacher(offering, teacher);

    post(
        "/grades/" + session.getId(),
        new RecordGradeRequest(student.getId(), new BigDecimal("8.00"), "Saisie initiale"),
        authHeaders(teacher),
        Grade.class);
    post(
        "/grades/" + session.getId(),
        new RecordGradeRequest(
            student.getId(), new BigDecimal("11.00"), "Erreur de transcription corrigee"),
        authHeaders(teacher),
        Grade.class);

    ResponseEntity<Grade[]> history =
        get(
            "/grades/history?examSessionId=" + session.getId() + "&studentId=" + student.getId(),
            authHeaders(admin),
            Grade[].class);

    assertThat(history.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(history.getBody()).hasSize(2);
    assertThat(history.getBody())
        .extracting(Grade::reason)
        .containsExactlyInAnyOrder("Saisie initiale", "Erreur de transcription corrigee");
  }

  @Test
  void currentGrades_asOwningStudent_isAllowed() {
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    var exam = createExam(offering, new BigDecimal("1.00"));
    var session = createExamSession(exam, teacher, group);
    assignTeacher(offering, teacher);
    post(
        "/grades/" + session.getId(),
        new RecordGradeRequest(student.getId(), new BigDecimal("15.00"), "Saisie"),
        authHeaders(teacher),
        Grade.class);

    ResponseEntity<Grade[]> response =
        get("/grades/current?studentId=" + student.getId(), authHeaders(student), Grade[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
  }

  @Test
  void currentGrades_asAnotherStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var otherStudent = createUser(UserRole.STUDENT);

    ResponseEntity<String> response =
        get("/grades/current?studentId=" + otherStudent.getId(), authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

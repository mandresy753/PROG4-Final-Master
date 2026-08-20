package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.RecordGradeRequest;
import com.example.demo.enums.Level;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Grade;
import com.example.demo.model.transcript.TranscriptStatus;
import com.example.demo.model.transcript.YearTranscript;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TranscriptControllerIT extends ControllerIT {

  @Test
  void yearTranscript_withOnlyPartOfExamsGraded_isMarkedProvisional() {
    var admin = createUser(UserRole.ADMIN);
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    createEnrollment(student, group, year, Level.L1, LocalDate.of(2025, 9, 1));
    assignTeacher(offering, teacher);

    var examCC = createExam(offering, new BigDecimal("0.40"));
    createExam(offering, new BigDecimal("0.60"));
    var sessionCC = createExamSession(examCC, teacher, group);

    post(
        "/grades/" + sessionCC.getId(),
        new RecordGradeRequest(student.getId(), new BigDecimal("15.00"), "CC"),
        authHeaders(teacher),
        Grade.class);

    ResponseEntity<YearTranscript> response =
        get(
            "/transcripts/year?studentId=" + student.getId() + "&academicYearId=" + year.getId(),
            authHeaders(admin),
            YearTranscript.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().status()).isEqualTo(TranscriptStatus.PROVISIONAL);
  }

  @Test
  void yearTranscript_withAllExamsGraded_isMarkedComplete() {
    var admin = createUser(UserRole.ADMIN);
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.TN, Semester.S5, 3);
    var year = createAcademicYear();
    var group = createGroup(Track.TN);
    var offering = createCourseOffering(course, year, group);
    createEnrollment(student, group, year, Level.L2, LocalDate.of(2025, 9, 1));
    assignTeacher(offering, teacher);

    var exam = createExam(offering, new BigDecimal("1.00"));
    var session = createExamSession(exam, teacher, group);
    post(
        "/grades/" + session.getId(),
        new RecordGradeRequest(student.getId(), new BigDecimal("13.00"), "Note unique"),
        authHeaders(teacher),
        Grade.class);

    ResponseEntity<YearTranscript> response =
        get(
            "/transcripts/year?studentId=" + student.getId() + "&academicYearId=" + year.getId(),
            authHeaders(admin),
            YearTranscript.class);

    assertThat(response.getBody().status()).isEqualTo(TranscriptStatus.COMPLETE);
  }

  @Test
  void yearTranscript_asOwningStudent_isAllowed() {
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S3, 4);
    var year = createAcademicYear();
    var group = createGroup(Track.EL);
    var offering = createCourseOffering(course, year, group);
    createEnrollment(student, group, year, Level.L1, LocalDate.of(2025, 9, 1));

    ResponseEntity<YearTranscript> response =
        get(
            "/transcripts/year?studentId=" + student.getId() + "&academicYearId=" + year.getId(),
            authHeaders(student),
            YearTranscript.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void yearTranscript_asAnotherStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var otherStudent = createUser(UserRole.STUDENT);
    var year = createAcademicYear();

    ResponseEntity<String> response =
        get(
            "/transcripts/year?studentId="
                + otherStudent.getId()
                + "&academicYearId="
                + year.getId(),
            authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void yearTranscript_asTeacher_isForbidden() {
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var year = createAcademicYear();

    ResponseEntity<String> response =
        get(
            "/transcripts/year?studentId=" + student.getId() + "&academicYearId=" + year.getId(),
            authHeaders(teacher));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void sendByEmail_asAdmin_triggersAsyncEventAndReturns202() {
    var admin = createUser(UserRole.ADMIN);
    var student = createUser(UserRole.STUDENT);

    ResponseEntity<Void> response =
        post(
            "/transcripts/" + student.getId() + "/send-email",
            null,
            authHeaders(admin),
            Void.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  void sendByEmail_asAnotherStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var otherStudent = createUser(UserRole.STUDENT);

    ResponseEntity<String> response =
        post("/transcripts/" + otherStudent.getId() + "/send-email", null, authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

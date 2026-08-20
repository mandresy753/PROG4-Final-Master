package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.RecordGradeRequest;
import com.example.demo.enums.Level;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Grade;
import com.example.demo.model.report.CourseAverage;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AverageControllerIT extends ControllerIT {

  @Test
  void courseAverage_weightsGradesByExamCoefficient() {
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
    var examFinal = createExam(offering, new BigDecimal("0.60"));
    var sessionCC = createExamSession(examCC, teacher, group);
    var sessionFinal = createExamSession(examFinal, teacher, group);

    post(
        "/grades/" + sessionCC.getId(),
        new RecordGradeRequest(student.getId(), new BigDecimal("10.00"), "CC"),
        authHeaders(teacher),
        Grade.class);
    post(
        "/grades/" + sessionFinal.getId(),
        new RecordGradeRequest(student.getId(), new BigDecimal("14.00"), "Final"),
        authHeaders(teacher),
        Grade.class);

    ResponseEntity<CourseAverage> response =
        get(
            "/averages/course?studentId="
                + student.getId()
                + "&courseOfferingId="
                + offering.getId(),
            authHeaders(admin),
            CourseAverage.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().average()).isEqualByComparingTo("12.40");
    assertThat(response.getBody().complete()).isTrue();
  }

  @Test
  void courseAverage_asOtherStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var otherStudent = createUser(UserRole.STUDENT);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var offering = createCourseOffering(course, year, createGroup(Track.EL));

    ResponseEntity<String> response =
        get(
            "/averages/course?studentId="
                + otherStudent.getId()
                + "&courseOfferingId="
                + offering.getId(),
            authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

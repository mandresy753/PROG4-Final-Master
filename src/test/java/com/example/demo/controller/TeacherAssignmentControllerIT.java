package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.AssignTeacherRequest;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.TeacherAssignment;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class TeacherAssignmentControllerIT extends ControllerIT {

  @Test
  void assign_asAdmin_allowsMultipleTeachersOnSameCourseOffering() {
    var admin = createUser(UserRole.ADMIN);
    var teacher1 = createUser(UserRole.TEACHER);
    var teacher2 = createUser(UserRole.TEACHER);
    var course = createCourse(Track.EL, Semester.S4, 4);
    var year = createAcademicYear();
    var offering = createCourseOffering(course, year, createGroup(Track.EL));

    post(
        "/teacher-assignments",
        new AssignTeacherRequest(offering.getId(), teacher1.getId()),
        authHeaders(admin),
        TeacherAssignment.class);
    ResponseEntity<TeacherAssignment> response =
        post(
            "/teacher-assignments",
            new AssignTeacherRequest(offering.getId(), teacher2.getId()),
            authHeaders(admin),
            TeacherAssignment.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<TeacherAssignment[]> byOffering =
        get(
            "/teacher-assignments?courseOfferingId=" + offering.getId(),
            authHeaders(admin),
            TeacherAssignment[].class);
    assertThat(byOffering.getBody()).hasSize(2);
  }

  @Test
  void assign_withNonTeacherUser_returns400() {
    var admin = createUser(UserRole.ADMIN);
    var student = createUser(UserRole.STUDENT);
    var course = createCourse(Track.TN, Semester.S5, 3);
    var year = createAcademicYear();
    var offering = createCourseOffering(course, year, createGroup(Track.TN));

    ResponseEntity<String> response =
        post(
            "/teacher-assignments",
            new AssignTeacherRequest(offering.getId(), student.getId()),
            authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void assign_asStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var teacher = createUser(UserRole.TEACHER);
    var course = createCourse(Track.EL, Semester.S3, 4);
    var year = createAcademicYear();
    var offering = createCourseOffering(course, year, createGroup(Track.EL));

    ResponseEntity<String> response =
        post(
            "/teacher-assignments",
            new AssignTeacherRequest(offering.getId(), teacher.getId()),
            authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

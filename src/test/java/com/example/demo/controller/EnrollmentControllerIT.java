package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.CreateEnrollmentRequest;
import com.example.demo.entity.JEnrollment;
import com.example.demo.enums.Level;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Enrollment;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class EnrollmentControllerIT extends ControllerIT {

  @Test
  void create_asAdmin_enrollsStudentInGroup() {
    var admin = createUser(UserRole.ADMIN);
    var student = createUser(UserRole.STUDENT);
    var group = createGroup(Track.EL);
    var year = createAcademicYear();

    var request =
        new CreateEnrollmentRequest(
            student.getId(), group.getId(), year.getId(), Level.L1, LocalDate.of(2025, 9, 1), null);

    ResponseEntity<Enrollment> response =
        post("/enrollments", request, authHeaders(admin), Enrollment.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().group().id()).isEqualTo(group.getId());
    assertThat(response.getBody().student().id()).isEqualTo(student.getId());
  }

  @Test
  void create_asTeacher_isForbidden() {
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var group = createGroup(Track.TN);
    var year = createAcademicYear();

    var request =
        new CreateEnrollmentRequest(
            student.getId(), group.getId(), year.getId(), Level.L1, LocalDate.of(2025, 9, 1), null);

    ResponseEntity<String> response = post("/enrollments", request, authHeaders(teacher));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void studentChangingGroupMidYear_keepsHistoryOfBothEnrollments() {

    var admin = createUser(UserRole.ADMIN);
    var student = createUser(UserRole.STUDENT);
    var year = createAcademicYear();
    var k1 = createGroup(Track.EL);
    var k3 = createGroup(Track.EL);

    createEnrollment(
        student, k1, year, Level.L1, LocalDate.of(2025, 9, 1), LocalDate.of(2025, 12, 20));
    createEnrollment(
        student, k3, year, Level.L1, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 3, 1));
    createEnrollment(student, k1, year, Level.L1, LocalDate.of(2026, 3, 2), null);

    ResponseEntity<Enrollment[]> response =
        get("/enrollments?studentId=" + student.getId(), authHeaders(admin), Enrollment[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(3);
    assertThat(response.getBody())
        .extracting(e -> e.group().id())
        .containsExactlyInAnyOrder(k1.getId(), k3.getId(), k1.getId());
  }

  @Test
  void findByStudent_asSameStudent_isAllowed() {
    var student = createUser(UserRole.STUDENT);
    var group = createGroup(Track.EL);
    var year = createAcademicYear();
    createEnrollment(student, group, year, Level.L1, LocalDate.of(2025, 9, 1));

    ResponseEntity<Enrollment[]> response =
        get("/enrollments?studentId=" + student.getId(), authHeaders(student), Enrollment[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
  }

  @Test
  void findByStudent_asDifferentStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var otherStudent = createUser(UserRole.STUDENT);

    ResponseEntity<String> response =
        get("/enrollments?studentId=" + otherStudent.getId(), authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void findByStudent_asTeacher_isAllowed() {
    var teacher = createUser(UserRole.TEACHER);
    var student = createUser(UserRole.STUDENT);
    var group = createGroup(Track.TN);
    var year = createAcademicYear();
    createEnrollment(student, group, year, Level.L2, LocalDate.of(2025, 9, 1));

    ResponseEntity<Enrollment[]> response =
        get("/enrollments?studentId=" + student.getId(), authHeaders(teacher), Enrollment[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void delete_asAdmin_removesEnrollment() {
    var admin = createUser(UserRole.ADMIN);
    var student = createUser(UserRole.STUDENT);
    var group = createGroup(Track.EL);
    var year = createAcademicYear();
    JEnrollment enrollment =
        createEnrollment(student, group, year, Level.L1, LocalDate.of(2025, 9, 1));

    ResponseEntity<Void> response =
        delete("/enrollments/" + enrollment.getId(), authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(enrollmentRepository.findById(enrollment.getId())).isEmpty();
  }
}

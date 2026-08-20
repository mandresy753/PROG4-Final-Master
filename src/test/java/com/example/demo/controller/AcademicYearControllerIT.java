package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.enums.UserRole;
import com.example.demo.model.AcademicYear;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AcademicYearControllerIT extends ControllerIT {

  @Test
  void findAll_withoutToken_returns401() {
    ResponseEntity<String> response = get("/academic-years", new HttpHeaders());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void findAll_asAuthenticatedStudent_returns200() {
    var year = createAcademicYear();
    var student = createUser(UserRole.STUDENT);

    ResponseEntity<AcademicYear[]> response =
        get("/academic-years", authHeaders(student), AcademicYear[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(List.of(response.getBody())).extracting(AcademicYear::id).contains(year.getId());
  }

  @Test
  void create_asAdmin_returns200AndPersists() {
    var admin = createUser(UserRole.ADMIN);
    var payload =
        AcademicYear.builder()
            .label("2030-2031-" + shortId())
            .startDate(LocalDate.of(2030, 9, 1))
            .endDate(LocalDate.of(2031, 7, 31))
            .build();

    ResponseEntity<AcademicYear> response =
        post("/academic-years", payload, authHeaders(admin), AcademicYear.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotNull();
    assertThat(academicYearRepository.findById(response.getBody().id())).isPresent();
  }

  @Test
  void create_asStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var payload =
        AcademicYear.builder()
            .label("2031-2032-" + shortId())
            .startDate(LocalDate.of(2031, 9, 1))
            .endDate(LocalDate.of(2032, 7, 31))
            .build();

    ResponseEntity<String> response = post("/academic-years", payload, authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void create_asTeacher_isForbidden() {
    var teacher = createUser(UserRole.TEACHER);
    var payload =
        AcademicYear.builder()
            .label("2032-2033-" + shortId())
            .startDate(LocalDate.of(2032, 9, 1))
            .endDate(LocalDate.of(2033, 7, 31))
            .build();

    ResponseEntity<String> response = post("/academic-years", payload, authHeaders(teacher));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void delete_asAdmin_removesRow() {
    var admin = createUser(UserRole.ADMIN);
    var year = createAcademicYear();

    ResponseEntity<Void> response = delete("/academic-years/" + year.getId(), authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(academicYearRepository.findById(year.getId())).isEmpty();
  }

  @Test
  void findById_unknownId_returns404() {
    var admin = createUser(UserRole.ADMIN);

    ResponseEntity<String> response =
        get("/academic-years/" + java.util.UUID.randomUUID(), authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}

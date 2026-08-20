package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Graduate;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GraduationControllerIT extends ControllerIT {

  @Test
  void listGraduates_withoutToken_returns401() {
    ResponseEntity<String> response =
        get("/graduates?track=EL&promotion=2025-2026", new HttpHeaders());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void listGraduates_forPromotionWithNoEligibleStudents_returnsEmptyList() {
    var admin = createUser(UserRole.ADMIN);

    ResponseEntity<Graduate[]> response =
        get(
            "/graduates?track=" + Track.EL + "&promotion=inexistante-" + shortId(),
            authHeaders(admin),
            Graduate[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  void listGraduates_asStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);

    ResponseEntity<String> response =
        get("/graduates?track=" + Track.TN + "&promotion=2025-2026", authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void listGraduates_asTeacher_isForbidden() {
    var teacher = createUser(UserRole.TEACHER);

    ResponseEntity<String> response =
        get("/graduates?track=" + Track.TN + "&promotion=2025-2026", authHeaders(teacher));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

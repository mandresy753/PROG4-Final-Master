package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.enums.UserRole;
import com.example.demo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UserControllerIT extends ControllerIT {

  @Test
  void findAll_asAdmin_returnsUsers() {
    var admin = createUser(UserRole.ADMIN);
    createUser(UserRole.STUDENT);

    ResponseEntity<User[]> response = get("/users", authHeaders(admin), User[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  void findAll_asStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);

    ResponseEntity<String> response = get("/users", authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void findAll_asTeacher_isForbidden() {
    var teacher = createUser(UserRole.TEACHER);

    ResponseEntity<String> response = get("/users", authHeaders(teacher));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void findByRole_asAdmin_filtersCorrectly() {
    var admin = createUser(UserRole.ADMIN);
    createUser(UserRole.TEACHER);
    createUser(UserRole.STUDENT);

    ResponseEntity<User[]> response = get("/users?role=TEACHER", authHeaders(admin), User[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .isNotEmpty()
        .allSatisfy(user -> assertThat(user.role()).isEqualTo(UserRole.TEACHER));
  }

  @Test
  void create_asAdmin_hashesPasswordAndGeneratesReference() {
    var admin = createUser(UserRole.ADMIN);
    var payload =
        User.builder()
            .firstName("Nouveau")
            .lastName("Etudiant")
            .email("nouveau." + shortId() + "@hei.test")
            .password("plainpassword")
            .role(UserRole.STUDENT)
            .build();

    ResponseEntity<User> response = post("/users", payload, authHeaders(admin), User.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotNull();
    assertThat(response.getBody().reference()).startsWith("STD");

    var stored = userRepository.findById(response.getBody().id()).orElseThrow();
    assertThat(stored.getPassword()).isNotEqualTo("plainpassword");
  }

  @Test
  void delete_unknownId_returns404() {
    var admin = createUser(UserRole.ADMIN);

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/users/" + java.util.UUID.randomUUID(),
            org.springframework.http.HttpMethod.DELETE,
            new org.springframework.http.HttpEntity<>(authHeaders(admin)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}

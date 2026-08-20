package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

class AuthControllerIT extends ControllerIT {

  @Test
  void login_withValidCredentials_returnsToken() {
    userRepository.save(
        com.example.demo.entity.JUser.builder()
            .reference("STD-" + shortId())
            .firstName("Jean")
            .lastName("Rakoto")
            .email("jean.rakoto@hei.test")
            .password(passwordEncoder.encode("secret123"))
            .role(UserRole.STUDENT)
            .build());

    ResponseEntity<LoginResponse> response =
        post(
            "/auth/login",
            new LoginRequest("jean.rakoto@hei.test", "secret123"),
            new org.springframework.http.HttpHeaders(),
            LoginResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().token()).isNotBlank();
    assertThat(response.getBody().role()).isEqualTo("STUDENT");
    assertThat(response.getBody().firstName()).isEqualTo("Jean");

    var headers = new org.springframework.http.HttpHeaders();
    headers.setBearerAuth(response.getBody().token());
    ResponseEntity<String> whoAmI = get("/academic-years", headers);
    assertThat(whoAmI.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void login_withWrongPassword_returns401() {
    userRepository.save(
        com.example.demo.entity.JUser.builder()
            .reference("STD-" + shortId())
            .firstName("Marie")
            .lastName("Rasoa")
            .email("marie.rasoa@hei.test")
            .password(passwordEncoder.encode("goodpassword"))
            .role(UserRole.STUDENT)
            .build());

    assertThatThrownBy(
            () ->
                post(
                    "/auth/login",
                    new LoginRequest("marie.rasoa@hei.test", "wrongpassword"),
                    new org.springframework.http.HttpHeaders()))
        .isInstanceOf(ResourceAccessException.class);
  }

  @Test
  void login_withUnknownEmail_returns401() {
    assertThatThrownBy(
            () ->
                post(
                    "/auth/login",
                    new LoginRequest("inconnu@hei.test", "whatever"),
                    new org.springframework.http.HttpHeaders()))
        .isInstanceOf(ResourceAccessException.class);
  }
}

package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(
        jwtService, "secretKey", "mySecretKeyThatIsLongEnoughForHmacSha256!!!");
    ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);
  }

  @Test
  void generateAndExtractUsername() {
    var user =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(UUID.randomUUID())
                .email("jean@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());

    var token = jwtService.generateToken(user);

    assertEquals("jean@test.com", jwtService.extractUsername(token));
  }

  @Test
  void generateAndExtractUserId() {
    var id = UUID.randomUUID();
    var user =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(id)
                .email("jean@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());

    var token = jwtService.generateToken(user);

    assertEquals(id, jwtService.extractUserId(token));
  }

  @Test
  void isTokenValid() {
    var user =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(UUID.randomUUID())
                .email("jean@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());

    var token = jwtService.generateToken(user);

    assertTrue(jwtService.isTokenValid(token, user));
  }

  @Test
  void isTokenInvalid_wrongUser() {
    var user1 =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(UUID.randomUUID())
                .email("jean@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());
    var user2 =
        AppUserPrincipal.of(
            com.example.demo.entity.JUser.builder()
                .id(UUID.randomUUID())
                .email("paul@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());

    var token = jwtService.generateToken(user1);

    assertFalse(jwtService.isTokenValid(token, user2));
  }

  @Test
  void extractUsername_invalidToken() {
    assertThrows(Exception.class, () -> jwtService.extractUsername("invalid-token"));
  }
}

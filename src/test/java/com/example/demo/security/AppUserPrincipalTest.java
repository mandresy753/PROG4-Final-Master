package com.example.demo.security;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppUserPrincipalTest {

  @Test
  void of() {
    var id = UUID.randomUUID();
    var user =
        com.example.demo.entity.JUser.builder()
            .id(id)
            .email("jean@test.com")
            .password("hash")
            .role(UserRole.STUDENT)
            .build();

    var principal = AppUserPrincipal.of(user);

    assertEquals(id, principal.getId());
    assertEquals(UserRole.STUDENT, principal.getRole());
    assertEquals("jean@test.com", principal.getUsername());
    assertEquals("hash", principal.getPassword());
    assertTrue(principal.isAccountNonExpired());
    assertTrue(principal.isAccountNonLocked());
    assertTrue(principal.isCredentialsNonExpired());
    assertTrue(principal.isEnabled());
    assertEquals(1, principal.getAuthorities().size());
    assertTrue(
        principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
  }

  @Test
  void of_teacher() {
    var user =
        com.example.demo.entity.JUser.builder()
            .id(UUID.randomUUID())
            .email("teacher@test.com")
            .password("hash")
            .role(UserRole.TEACHER)
            .build();

    var principal = AppUserPrincipal.of(user);

    assertTrue(
        principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER")));
  }

  @Test
  void of_admin() {
    var user =
        com.example.demo.entity.JUser.builder()
            .id(UUID.randomUUID())
            .email("admin@test.com")
            .password("hash")
            .role(UserRole.ADMIN)
            .build();

    var principal = AppUserPrincipal.of(user);

    assertTrue(
        principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
  }
}

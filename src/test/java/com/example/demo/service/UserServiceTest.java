package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.UserRole;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private UserMapper userMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserReferenceGenerator userReferenceGenerator;
  @InjectMocks private UserService userService;

  private User buildUser(UUID id) {
    return User.builder()
        .id(id)
        .reference("STD0001")
        .lastName("Rakoto")
        .firstName("Jean")
        .email("jean@test.com")
        .password(null)
        .role(UserRole.STUDENT)
        .build();
  }

  private com.example.demo.entity.JUser buildEntity(UUID id) {
    return com.example.demo.entity.JUser.builder()
        .id(id)
        .reference("STD0001")
        .lastName("Rakoto")
        .firstName("Jean")
        .email("jean@test.com")
        .password("hashed")
        .role(UserRole.STUDENT)
        .build();
  }

  @Test
  void findAll() {
    var entity = buildEntity(UUID.randomUUID());
    when(userRepository.findAll()).thenReturn(List.of(entity));
    when(userMapper.toModel(entity)).thenReturn(buildUser(entity.getId()));

    var result = userService.findAll();
    assertEquals(1, result.size());
    assertNull(result.get(0).password());
  }

  @Test
  void findByRole() {
    var entity = buildEntity(UUID.randomUUID());
    when(userRepository.findAll()).thenReturn(List.of(entity));
    when(userMapper.toModel(entity)).thenReturn(buildUser(entity.getId()));

    var result = userService.findByRole(UserRole.STUDENT);
    assertEquals(1, result.size());
  }

  @Test
  void findById_found() {
    var id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.of(buildEntity(id)));
    when(userMapper.toModel(any())).thenReturn(buildUser(id));

    var result = userService.findById(id);
    assertEquals("STD0001", result.reference());
    assertNull(result.password());
  }

  @Test
  void findById_notFound() {
    var id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> userService.findById(id));
  }

  @Test
  void create() {
    when(userReferenceGenerator.generate(UserRole.STUDENT)).thenReturn("STD0001");
    when(passwordEncoder.encode("pass")).thenReturn("hashed");
    var entity = buildEntity(UUID.randomUUID());
    when(userMapper.toEntity(any())).thenReturn(entity);
    when(userRepository.save(any())).thenReturn(entity);
    when(userMapper.toModel(entity)).thenReturn(buildUser(entity.getId()));

    var result =
        userService.create(
            User.builder()
                .lastName("Rakoto")
                .firstName("Jean")
                .email("jean@test.com")
                .password("pass")
                .role(UserRole.STUDENT)
                .build());

    assertEquals("STD0001", result.reference());
    assertNull(result.password());
  }

  @Test
  void update_withPassword() {
    var id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.of(buildEntity(id)));
    when(passwordEncoder.encode("newpass")).thenReturn("newhashed");
    var updated = buildEntity(id);
    updated.setPassword("newhashed");
    when(userMapper.toEntity(any())).thenReturn(updated);
    when(userRepository.save(any())).thenReturn(updated);
    when(userMapper.toModel(any())).thenReturn(buildUser(id));

    var result =
        userService.update(
            id,
            User.builder()
                .lastName("Rakoto")
                .firstName("Jean")
                .email("jean@test.com")
                .password("newpass")
                .role(UserRole.STUDENT)
                .build());

    assertEquals(id, result.id());
    assertNull(result.password());
  }

  @Test
  void update_withoutPassword() {
    var id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.of(buildEntity(id)));
    when(userMapper.toEntity(any())).thenReturn(buildEntity(id));
    when(userRepository.save(any())).thenReturn(buildEntity(id));
    when(userMapper.toModel(any())).thenReturn(buildUser(id));

    var result =
        userService.update(
            id,
            User.builder()
                .lastName("Rakoto")
                .firstName("Jean")
                .email("jean@test.com")
                .password(null)
                .role(UserRole.STUDENT)
                .build());

    assertEquals(id, result.id());
  }

  @Test
  void update_notFound() {
    var id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> userService.update(id, buildUser(null)));
  }

  @Test
  void delete() {
    var id = UUID.randomUUID();
    when(userRepository.existsById(id)).thenReturn(true);
    userService.delete(id);
    verify(userRepository).deleteById(id);
  }

  @Test
  void delete_notFound() {
    var id = UUID.randomUUID();
    when(userRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> userService.delete(id));
  }
}

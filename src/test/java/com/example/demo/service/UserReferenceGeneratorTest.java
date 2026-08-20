package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.UserRole;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserReferenceGeneratorTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private UserReferenceGenerator userReferenceGenerator;

  @Test
  void generate_firstStudent() {
    when(userRepository.findFirstByReferenceStartingWithOrderByReferenceDesc("STD"))
        .thenReturn(Optional.empty());

    assertEquals("STD0001", userReferenceGenerator.generate(UserRole.STUDENT));
  }

  @Test
  void generate_nextStudent() {
    var entity = com.example.demo.entity.JUser.builder().reference("STD0005").build();
    when(userRepository.findFirstByReferenceStartingWithOrderByReferenceDesc("STD"))
        .thenReturn(Optional.of(entity));

    assertEquals("STD0006", userReferenceGenerator.generate(UserRole.STUDENT));
  }

  @Test
  void generate_firstTeacher() {
    when(userRepository.findFirstByReferenceStartingWithOrderByReferenceDesc("TCH"))
        .thenReturn(Optional.empty());

    assertEquals("TCH0001", userReferenceGenerator.generate(UserRole.TEACHER));
  }

  @Test
  void generate_firstAdmin() {
    when(userRepository.findFirstByReferenceStartingWithOrderByReferenceDesc("ADM"))
        .thenReturn(Optional.empty());

    assertEquals("ADM0001", userReferenceGenerator.generate(UserRole.ADMIN));
  }
}

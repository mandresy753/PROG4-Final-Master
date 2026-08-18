package com.example.demo.service;

import com.example.demo.enums.UserRole;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final UserReferenceGenerator userReferenceGenerator;

  public List<User> findAll() {
    return userRepository.findAll().stream()
        .map(userMapper::toModel)
        .map(this::withoutPassword)
        .toList();
  }

  public List<User> findByRole(UserRole role) {
    return findAll().stream().filter(user -> user.role() == role).toList();
  }

  public User findById(UUID id) {
    return userRepository
        .findById(id)
        .map(userMapper::toModel)
        .map(this::withoutPassword)
        .orElseThrow(() -> ResourceNotFoundException.of("User", id));
  }

  public User create(User user) {
    var entity =
        userMapper.toEntity(
            new User(
                null,
                userReferenceGenerator.generate(user.role()),
                user.lastName(),
                user.firstName(),
                user.email(),
                passwordEncoder.encode(user.password()),
                user.role()));

    var saved = userRepository.save(entity);
    return withoutPassword(userMapper.toModel(saved));
  }

  public User update(UUID id, User user) {
    var existing =
        userRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("User", id));

    String password;

    if (user.password() == null || user.password().isBlank()) {
      password = existing.getPassword();
    } else {
      password = passwordEncoder.encode(user.password());
    }

    var entity =
        userMapper.toEntity(
            new User(
                id,
                existing.getReference(),
                user.lastName(),
                user.firstName(),
                user.email(),
                password,
                user.role()));

    var saved = userRepository.save(entity);
    return withoutPassword(userMapper.toModel(saved));
  }

  public void delete(UUID id) {
    if (!userRepository.existsById(id)) {
      throw ResourceNotFoundException.of("User", id);
    }

    userRepository.deleteById(id);
  }

  private User withoutPassword(User user) {
    return new User(
        user.id(),
        user.reference(),
        user.lastName(),
        user.firstName(),
        user.email(),
        null,
        user.role());
  }
}

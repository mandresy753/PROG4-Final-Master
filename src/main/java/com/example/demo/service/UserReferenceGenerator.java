package com.example.demo.service;

import com.example.demo.enums.UserRole;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserReferenceGenerator {

  private static final int SEQUENCE_DIGITS = 4;

  private final UserRepository userRepository;

  public String generate(UserRole role) {
    var prefix = role.referencePrefix();

    var nextSequence =
        userRepository
            .findFirstByReferenceStartingWithOrderByReferenceDesc(prefix)
            .map(user -> extractSequence(user.getReference(), prefix) + 1)
            .orElse(1);

    return prefix + String.format("%0" + SEQUENCE_DIGITS + "d", nextSequence);
  }

  private int extractSequence(String reference, String prefix) {
    try {
      return Integer.parseInt(reference.substring(prefix.length()));
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}

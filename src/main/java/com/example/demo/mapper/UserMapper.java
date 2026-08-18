package com.example.demo.mapper;

import com.example.demo.entity.JUser;
import com.example.demo.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public User toModel(JUser entity) {
    return User.builder()
        .id(entity.getId())
        .reference(entity.getReference())
        .lastName(entity.getLastName())
        .firstName(entity.getFirstName())
        .email(entity.getEmail())
        .password(entity.getPassword())
        .role(entity.getRole())
        .build();
  }

  public JUser toEntity(User model) {
    return JUser.builder()
        .id(model.id())
        .reference(model.reference())
        .lastName(model.lastName())
        .firstName(model.firstName())
        .email(model.email())
        .password(model.password())
        .role(model.role())
        .build();
  }
}

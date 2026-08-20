package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.entity.JUser;
import com.example.demo.enums.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  private final UserMapper userMapper = new UserMapper();

  @Test
  void toModelAndBack() {
    var id = UUID.randomUUID();
    var entity =
        JUser.builder()
            .id(id)
            .reference("STD0001")
            .lastName("Rakoto")
            .firstName("Jean")
            .email("jean@test.com")
            .password("hash")
            .role(UserRole.STUDENT)
            .build();

    var model = userMapper.toModel(entity);

    assertEquals(id, model.id());
    assertEquals("STD0001", model.reference());
    assertEquals("Rakoto", model.lastName());
    assertEquals("Jean", model.firstName());
    assertEquals("jean@test.com", model.email());
    assertEquals("hash", model.password());
    assertEquals(UserRole.STUDENT, model.role());

    var backToEntity = userMapper.toEntity(model);

    assertEquals(id, backToEntity.getId());
    assertEquals("STD0001", backToEntity.getReference());
  }
}

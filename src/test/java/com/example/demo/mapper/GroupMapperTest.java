package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.entity.JGroup;
import com.example.demo.enums.Track;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupMapperTest {

  private final GroupMapper groupMapper = new GroupMapper();

  @Test
  void toModelAndBack() {
    var id = UUID.randomUUID();
    var entity = JGroup.builder().id(id).reference("K1").track(Track.EL).build();

    var model = groupMapper.toModel(entity);

    assertEquals(id, model.id());
    assertEquals("K1", model.reference());
    assertEquals(Track.EL, model.track());

    var backToEntity = groupMapper.toEntity(model);

    assertEquals(id, backToEntity.getId());
    assertEquals("K1", backToEntity.getReference());
    assertEquals(Track.EL, backToEntity.getTrack());
  }
}

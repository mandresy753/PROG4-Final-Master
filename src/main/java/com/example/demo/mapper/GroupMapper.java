package com.example.demo.mapper;

import com.example.demo.entity.JGroup;
import com.example.demo.model.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

  public Group toModel(JGroup entity) {
    return Group.builder()
        .id(entity.getId())
        .reference(entity.getReference())
        .track(entity.getTrack())
        .build();
  }

  public JGroup toEntity(Group model) {
    return JGroup.builder()
        .id(model.id())
        .reference(model.reference())
        .track(model.track())
        .build();
  }
}

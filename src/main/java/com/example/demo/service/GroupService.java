package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.GroupMapper;
import com.example.demo.model.Group;
import com.example.demo.repository.GroupRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GroupService {

  private final GroupRepository groupRepository;
  private final GroupMapper groupMapper;

  public List<Group> findAll() {
    return groupRepository.findAll().stream().map(groupMapper::toModel).toList();
  }

  public Group findById(UUID id) {
    return groupRepository
        .findById(id)
        .map(groupMapper::toModel)
        .orElseThrow(() -> ResourceNotFoundException.of("Group", id));
  }

  public Group create(Group group) {
    var entity = groupMapper.toEntity(new Group(null, group.reference(), group.track()));

    var saved = groupRepository.save(entity);

    return groupMapper.toModel(saved);
  }

  public Group update(UUID id, Group group) {
    if (!groupRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Group", id);
    }

    var entity = groupMapper.toEntity(new Group(id, group.reference(), group.track()));

    var saved = groupRepository.save(entity);

    return groupMapper.toModel(saved);
  }

  public void delete(UUID id) {
    if (!groupRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Group", id);
    }

    groupRepository.deleteById(id);
  }
}

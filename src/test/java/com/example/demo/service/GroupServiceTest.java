package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.Track;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.GroupMapper;
import com.example.demo.model.Group;
import com.example.demo.repository.GroupRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

  @Mock private GroupRepository groupRepository;
  @Mock private GroupMapper groupMapper;
  @InjectMocks private GroupService groupService;

  private Group buildGroup(UUID id) {
    return Group.builder().id(id).reference("K1").track(Track.TRONC_COMMUN).build();
  }

  @Test
  void findAll() {
    var entity =
        com.example.demo.entity.JGroup.builder()
            .id(UUID.randomUUID())
            .reference("K1")
            .track(Track.TRONC_COMMUN)
            .build();
    when(groupRepository.findAll()).thenReturn(List.of(entity));
    when(groupMapper.toModel(entity)).thenReturn(buildGroup(entity.getId()));

    var result = groupService.findAll();

    assertEquals(1, result.size());
  }

  @Test
  void findById_found() {
    var id = UUID.randomUUID();
    var entity =
        com.example.demo.entity.JGroup.builder()
            .id(id)
            .reference("K1")
            .track(Track.TRONC_COMMUN)
            .build();
    when(groupRepository.findById(id)).thenReturn(Optional.of(entity));
    when(groupMapper.toModel(entity)).thenReturn(buildGroup(id));

    assertEquals("K1", groupService.findById(id).reference());
  }

  @Test
  void findById_notFound() {
    var id = UUID.randomUUID();
    when(groupRepository.findById(id)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> groupService.findById(id));
  }

  @Test
  void create() {
    when(groupMapper.toEntity(any()))
        .thenReturn(
            com.example.demo.entity.JGroup.builder()
                .reference("K1")
                .track(Track.TRONC_COMMUN)
                .build());
    var saved =
        com.example.demo.entity.JGroup.builder()
            .id(UUID.randomUUID())
            .reference("K1")
            .track(Track.TRONC_COMMUN)
            .build();
    when(groupRepository.save(any())).thenReturn(saved);
    when(groupMapper.toModel(saved)).thenReturn(buildGroup(saved.getId()));

    var result = groupService.create(buildGroup(null));

    assertEquals("K1", result.reference());
  }

  @Test
  void update() {
    var id = UUID.randomUUID();
    when(groupRepository.existsById(id)).thenReturn(true);
    when(groupMapper.toEntity(any()))
        .thenReturn(
            com.example.demo.entity.JGroup.builder()
                .id(id)
                .reference("K1")
                .track(Track.TRONC_COMMUN)
                .build());
    var saved =
        com.example.demo.entity.JGroup.builder()
            .id(id)
            .reference("K1")
            .track(Track.TRONC_COMMUN)
            .build();
    when(groupRepository.save(any())).thenReturn(saved);
    when(groupMapper.toModel(saved)).thenReturn(buildGroup(id));

    assertEquals(id, groupService.update(id, buildGroup(null)).id());
  }

  @Test
  void update_notFound() {
    var id = UUID.randomUUID();
    when(groupRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> groupService.update(id, buildGroup(null)));
  }

  @Test
  void delete() {
    var id = UUID.randomUUID();
    when(groupRepository.existsById(id)).thenReturn(true);
    groupService.delete(id);
    verify(groupRepository).deleteById(id);
  }

  @Test
  void delete_notFound() {
    var id = UUID.randomUUID();
    when(groupRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> groupService.delete(id));
  }
}

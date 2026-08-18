package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.Group;
import com.example.demo.service.GroupService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/groups")
@AllArgsConstructor
public class GroupController {

  private final GroupService groupService;

  @GetMapping
  public List<Group> findAll() {
    return groupService.findAll();
  }

  @GetMapping("/{id}")
  public Group findById(@PathVariable UUID id) {
    return groupService.findById(id);
  }

  @PostMapping
  public Group create(@RequestBody Group group) {
    return groupService.create(group);
  }

  @PutMapping("/{id}")
  public Group update(@PathVariable UUID id, @RequestBody Group group) {
    return groupService.update(id, group);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    groupService.delete(id);
  }
}

package com.example.demo.endpoint.rest.controller;

import com.example.demo.enums.UserRole;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping
  public List<User> findAll(@RequestParam(required = false) UserRole role) {
    return role == null ? userService.findAll() : userService.findByRole(role);
  }

  @GetMapping("/{id}")
  public User findById(@PathVariable UUID id) {
    return userService.findById(id);
  }

  @PostMapping
  public User create(@RequestBody User user) {
    return userService.create(user);
  }

  @PutMapping("/{id}")
  public User update(@PathVariable UUID id, @RequestBody User user) {
    return userService.update(id, user);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    userService.delete(id);
  }
}

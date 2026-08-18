package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.Course;
import com.example.demo.service.CourseService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@AllArgsConstructor
public class CourseController {

  private final CourseService courseService;

  @GetMapping
  public List<Course> findAll() {
    return courseService.findAll();
  }

  @GetMapping("/{id}")
  public Course findById(@PathVariable UUID id) {
    return courseService.findById(id);
  }

  @PostMapping
  public Course create(@RequestBody Course course) {
    return courseService.create(course);
  }

  @PutMapping("/{id}")
  public Course update(@PathVariable UUID id, @RequestBody Course course) {
    return courseService.update(id, course);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    courseService.delete(id);
  }
}

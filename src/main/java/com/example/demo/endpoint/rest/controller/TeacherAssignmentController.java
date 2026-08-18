package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.AssignTeacherRequest;
import com.example.demo.model.TeacherAssignment;
import com.example.demo.service.TeacherAssignmentService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teacher-assignments")
@AllArgsConstructor
public class TeacherAssignmentController {

  private final TeacherAssignmentService teacherAssignmentService;

  @GetMapping
  public List<TeacherAssignment> find(
      @RequestParam(required = false) UUID teacherId,
      @RequestParam(required = false) UUID courseOfferingId) {
    if (teacherId != null) {
      return teacherAssignmentService.findByTeacher(teacherId);
    }
    if (courseOfferingId != null) {
      return teacherAssignmentService.findByCourseOffering(courseOfferingId);
    }
    return List.of();
  }

  @PostMapping
  public TeacherAssignment assign(@RequestBody AssignTeacherRequest request) {
    return teacherAssignmentService.assign(request.courseOfferingId(), request.teacherId());
  }

  @DeleteMapping("/{id}")
  public void unassign(@PathVariable UUID id) {
    teacherAssignmentService.unassign(id);
  }
}

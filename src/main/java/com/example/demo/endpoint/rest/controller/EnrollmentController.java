package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.CreateEnrollmentRequest;
import com.example.demo.enums.Track;
import com.example.demo.model.Enrollment;
import com.example.demo.service.EnrollmentService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enrollments")
@AllArgsConstructor
public class EnrollmentController {

  private final EnrollmentService enrollmentService;

  @GetMapping
  public List<Enrollment> find(
      @RequestParam(required = false) UUID studentId,
      @RequestParam(required = false) UUID groupId,
      @RequestParam(required = false) UUID academicYearId) {
    if (studentId != null) {
      return enrollmentService.findByStudent(studentId);
    }
    if (groupId != null && academicYearId != null) {
      return enrollmentService.findByGroupAndAcademicYear(groupId, academicYearId);
    }
    return List.of();
  }

  @GetMapping("/track")
  public Track track(
      @RequestParam UUID studentId, @RequestParam(required = false) UUID academicYearId) {
    return academicYearId != null
        ? enrollmentService.trackForYear(studentId, academicYearId)
        : enrollmentService.currentTrack(studentId);
  }

  @PostMapping
  public Enrollment create(@RequestBody CreateEnrollmentRequest request) {
    return enrollmentService.create(
        request.studentId(),
        request.groupId(),
        request.academicYearId(),
        request.level(),
        request.startDate(),
        request.endDate());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    enrollmentService.delete(id);
  }
}

package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.CreateCourseOfferingRequest;
import com.example.demo.model.CourseOffering;
import com.example.demo.service.CourseOfferingService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course-offerings")
@AllArgsConstructor
public class CourseOfferingController {

  private final CourseOfferingService courseOfferingService;

  @GetMapping
  public List<CourseOffering> findAll(
      @RequestParam(required = false) UUID academicYearId,
      @RequestParam(required = false) UUID groupId) {
    if (academicYearId != null && groupId != null) {
      return courseOfferingService.findByGroupAndAcademicYear(groupId, academicYearId);
    }
    if (academicYearId != null) {
      return courseOfferingService.findByAcademicYear(academicYearId);
    }
    return courseOfferingService.findAll();
  }

  @GetMapping("/{id}")
  public CourseOffering findById(@PathVariable UUID id) {
    return courseOfferingService.findById(id);
  }

  @PostMapping
  public CourseOffering create(@RequestBody CreateCourseOfferingRequest request) {
    return courseOfferingService.create(
        request.courseId(), request.academicYearId(), request.groupIds());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    courseOfferingService.delete(id);
  }
}

package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.CreateExamRequest;
import com.example.demo.model.Exam;
import com.example.demo.service.ExamService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exams")
@AllArgsConstructor
public class ExamController {

  private final ExamService examService;

  @GetMapping
  public List<Exam> findByCourseOffering(@RequestParam UUID courseOfferingId) {
    return examService.findByCourseOffering(courseOfferingId);
  }

  @GetMapping("/{id}")
  public Exam findById(@PathVariable UUID id) {
    return examService.findById(id);
  }

  @PostMapping
  public Exam create(@RequestBody CreateExamRequest request) {
    return examService.create(request.courseOfferingId(), request.coefficient());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    examService.delete(id);
  }
}

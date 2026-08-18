package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.AcademicYear;
import com.example.demo.service.AcademicYearService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/academic-years")
@AllArgsConstructor
public class AcademicYearController {

  private final AcademicYearService academicYearService;

  @GetMapping
  public List<AcademicYear> findAll() {
    return academicYearService.findAll();
  }

  @GetMapping("/{id}")
  public AcademicYear findById(@PathVariable UUID id) {
    return academicYearService.findById(id);
  }

  @PostMapping
  public AcademicYear create(@RequestBody AcademicYear academicYear) {
    return academicYearService.create(academicYear);
  }

  @PutMapping("/{id}")
  public AcademicYear update(@PathVariable UUID id, @RequestBody AcademicYear academicYear) {
    return academicYearService.update(id, academicYear);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    academicYearService.delete(id);
  }
}

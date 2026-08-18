package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.RecordGradeRequest;
import com.example.demo.model.Grade;
import com.example.demo.security.AppUserPrincipal;
import com.example.demo.service.GradeService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grades")
@AllArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @GetMapping("/current")
  public List<Grade> currentForStudent(@RequestParam UUID studentId) {
    return gradeService.currentGradesForStudent(studentId);
  }

  @GetMapping("/history")
  public List<Grade> history(@RequestParam UUID examSessionId, @RequestParam UUID studentId) {

    return gradeService.history(examSessionId, studentId);
  }

  @PostMapping("/{examSessionId}")
  public Grade record(
      @PathVariable UUID examSessionId,
      @RequestBody RecordGradeRequest request,
      @AuthenticationPrincipal AppUserPrincipal me) {

    return gradeService.record(
        examSessionId, request.studentId(), me.getId(), request.value(), request.reason());
  }
}

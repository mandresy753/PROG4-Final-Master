package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.CreateExamSessionRequest;
import com.example.demo.model.ExamSession;
import com.example.demo.service.ExamSessionService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exam-sessions")
@AllArgsConstructor
public class ExamSessionController {

  private final ExamSessionService examSessionService;

  @GetMapping
  public List<ExamSession> findByExam(@RequestParam UUID examId) {
    return examSessionService.findByExam(examId);
  }

  @PostMapping
  public ExamSession create(@RequestBody CreateExamSessionRequest request) {
    return examSessionService.create(
        request.examId(), request.examDate(), request.teacherId(), request.groupIds());
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    examSessionService.delete(id);
  }
}

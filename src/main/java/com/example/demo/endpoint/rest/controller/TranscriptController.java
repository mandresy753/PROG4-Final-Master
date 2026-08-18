package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.TranscriptEmailRequested;
import com.example.demo.model.transcript.FullTranscript;
import com.example.demo.model.transcript.YearTranscript;
import com.example.demo.service.TranscriptService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transcripts")
@AllArgsConstructor
public class TranscriptController {

  private final TranscriptService transcriptService;
  private final EventProducer<TranscriptEmailRequested> transcriptEmailEventProducer;

  @GetMapping("/year")
  public YearTranscript year(@RequestParam UUID studentId, @RequestParam UUID academicYearId) {
    return transcriptService.yearTranscript(studentId, academicYearId);
  }

  @GetMapping("/full")
  public FullTranscript full(@RequestParam UUID studentId) {
    return transcriptService.fullTranscript(studentId);
  }

  @PostMapping("/{studentId}/send-email")
  public ResponseEntity<Void> sendByEmail(@PathVariable UUID studentId) {
    transcriptEmailEventProducer.accept(
        List.of(TranscriptEmailRequested.builder().studentId(studentId).build()));

    return ResponseEntity.accepted().build();
  }
}

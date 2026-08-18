package com.example.demo.endpoint.rest.controller;

import com.example.demo.enums.Track;
import com.example.demo.model.Graduate;
import com.example.demo.service.GraduateExportService;
import com.example.demo.service.GraduationService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/graduates")
@AllArgsConstructor
public class GraduationController {

  private final GraduationService graduationService;
  private final GraduateExportService graduateExportService;

  @GetMapping
  public List<Graduate> listGraduates(@RequestParam Track track, @RequestParam String promotion) {
    return graduationService.listGraduates(track, promotion);
  }

  @GetMapping("/export")
  public String exportGraduates(@RequestParam String promotion) {
    return graduateExportService.exportToXlsx(promotion);
  }
}

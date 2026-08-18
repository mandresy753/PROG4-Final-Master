package com.example.demo.endpoint.web;

import com.example.demo.service.GraduateExportService;
import com.example.demo.service.GraduationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/promotions")
@AllArgsConstructor
public class GraduationViewController {

  private final GraduationService graduationService;
  private final GraduateExportService graduateExportService;

  @GetMapping
  public String list(Model model) {
    model.addAttribute("promotions", graduationService.listPromotions());
    return "promotions";
  }

  @GetMapping("/{promotion}/export")
  public String export(@PathVariable String promotion) {
    var downloadUrl = graduateExportService.exportToXlsx(promotion);
    return "redirect:" + downloadUrl;
  }
}

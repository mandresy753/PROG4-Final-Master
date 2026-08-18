package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.report.CourseAverage;
import com.example.demo.model.report.OverallAverage;
import com.example.demo.model.report.YearAverage;
import com.example.demo.service.GradeAverageService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/averages")
@AllArgsConstructor
public class AverageController {

  private final GradeAverageService gradeAverageService;

  @GetMapping("/course")
  public CourseAverage course(@RequestParam UUID studentId, @RequestParam UUID courseOfferingId) {
    return gradeAverageService.courseAverage(studentId, courseOfferingId);
  }

  @GetMapping("/year")
  public YearAverage year(@RequestParam UUID studentId, @RequestParam UUID academicYearId) {
    return gradeAverageService.yearAverage(studentId, academicYearId);
  }

  @GetMapping("/overall")
  public OverallAverage overall(@RequestParam UUID studentId) {
    return gradeAverageService.overallAverage(studentId);
  }
}

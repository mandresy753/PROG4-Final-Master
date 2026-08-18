package com.example.demo.mapper;

import com.example.demo.entity.JExam;
import com.example.demo.model.Exam;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExamMapper {

  private final CourseOfferingMapper courseOfferingMapper;

  public Exam toModel(JExam entity) {
    return Exam.builder()
        .id(entity.getId())
        .courseOffering(courseOfferingMapper.toModel(entity.getCourseOffering()))
        .coefficient(entity.getCoefficient())
        .build();
  }

  public JExam toEntity(Exam model) {
    return JExam.builder()
        .id(model.id())
        .courseOffering(courseOfferingMapper.toEntity(model.courseOffering()))
        .coefficient(model.coefficient())
        .build();
  }
}

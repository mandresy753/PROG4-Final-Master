package com.example.demo.mapper;

import com.example.demo.entity.JAcademicYear;
import com.example.demo.model.AcademicYear;
import org.springframework.stereotype.Component;

@Component
public class AcademicYearMapper {

  public AcademicYear toModel(JAcademicYear entity) {
    return AcademicYear.builder()
        .id(entity.getId())
        .label(entity.getLabel())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .build();
  }

  public JAcademicYear toEntity(AcademicYear model) {
    return JAcademicYear.builder()
        .id(model.id())
        .label(model.label())
        .startDate(model.startDate())
        .endDate(model.endDate())
        .build();
  }
}

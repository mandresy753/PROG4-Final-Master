package com.example.demo.mapper;

import com.example.demo.entity.JEnrollment;
import com.example.demo.model.Enrollment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EnrollmentMapper {

  private final UserMapper userMapper;
  private final GroupMapper groupMapper;
  private final AcademicYearMapper academicYearMapper;

  public Enrollment toModel(JEnrollment entity) {
    return Enrollment.builder()
        .id(entity.getId())
        .student(userMapper.toModel(entity.getStudent()))
        .group(groupMapper.toModel(entity.getGroup()))
        .academicYear(academicYearMapper.toModel(entity.getAcademicYear()))
        .level(entity.getLevel())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .build();
  }

  public JEnrollment toEntity(Enrollment model) {
    return JEnrollment.builder()
        .id(model.id())
        .student(userMapper.toEntity(model.student()))
        .group(groupMapper.toEntity(model.group()))
        .academicYear(academicYearMapper.toEntity(model.academicYear()))
        .level(model.level())
        .startDate(model.startDate())
        .endDate(model.endDate())
        .build();
  }
}

package com.example.demo.mapper;

import com.example.demo.entity.JCourseOffering;
import com.example.demo.model.CourseOffering;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CourseOfferingMapper {

  private final CourseMapper courseMapper;
  private final AcademicYearMapper academicYearMapper;
  private final GroupMapper groupMapper;

  public CourseOffering toModel(JCourseOffering entity) {
    return CourseOffering.builder()
        .id(entity.getId())
        .course(courseMapper.toModel(entity.getCourse()))
        .academicYear(academicYearMapper.toModel(entity.getAcademicYear()))
        .groups(entity.getGroups().stream().map(groupMapper::toModel).toList())
        .build();
  }

  public JCourseOffering toEntity(CourseOffering model) {
    return JCourseOffering.builder()
        .id(model.id())
        .course(courseMapper.toEntity(model.course()))
        .academicYear(academicYearMapper.toEntity(model.academicYear()))
        .groups(
            model.groups() == null
                ? new java.util.HashSet<>()
                : model.groups().stream()
                    .map(groupMapper::toEntity)
                    .collect(java.util.stream.Collectors.toSet()))
        .build();
  }
}

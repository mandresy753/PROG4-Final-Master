package com.example.demo.mapper;

import com.example.demo.entity.JTeacherAssignment;
import com.example.demo.model.TeacherAssignment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TeacherAssignmentMapper {

  private final CourseOfferingMapper courseOfferingMapper;
  private final UserMapper userMapper;

  public TeacherAssignment toModel(JTeacherAssignment entity) {
    return TeacherAssignment.builder()
        .id(entity.getId())
        .courseOffering(courseOfferingMapper.toModel(entity.getCourseOffering()))
        .teacher(userMapper.toModel(entity.getTeacher()))
        .build();
  }

  public JTeacherAssignment toEntity(TeacherAssignment model) {
    return JTeacherAssignment.builder()
        .id(model.id())
        .courseOffering(courseOfferingMapper.toEntity(model.courseOffering()))
        .teacher(userMapper.toEntity(model.teacher()))
        .build();
  }
}

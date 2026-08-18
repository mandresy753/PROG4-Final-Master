package com.example.demo.mapper;

import com.example.demo.entity.JCourse;
import com.example.demo.model.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

  public Course toModel(JCourse entity) {
    return Course.builder()
        .id(entity.getId())
        .ref(entity.getRef())
        .title(entity.getTitle())
        .creditCount(entity.getCreditCount())
        .track(entity.getTrack())
        .semester(entity.getSemester())
        .build();
  }

  public JCourse toEntity(Course model) {
    return JCourse.builder()
        .id(model.id())
        .ref(model.ref())
        .title(model.title())
        .creditCount(model.creditCount())
        .track(model.track())
        .semester(model.semester())
        .build();
  }
}

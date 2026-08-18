package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.Course;
import com.example.demo.repository.CourseRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;

  public List<Course> findAll() {
    return courseRepository.findAll().stream().map(courseMapper::toModel).toList();
  }

  public Course findById(UUID id) {
    return courseRepository
        .findById(id)
        .map(courseMapper::toModel)
        .orElseThrow(() -> ResourceNotFoundException.of("Course", id));
  }

  public Course create(Course course) {
    var newCourse =
        Course.builder()
            .id(null)
            .ref(course.ref())
            .title(course.title())
            .creditCount(course.creditCount())
            .track(course.track())
            .semester(course.semester())
            .build();

    var saved = courseRepository.save(courseMapper.toEntity(newCourse));

    return courseMapper.toModel(saved);
  }

  public Course update(UUID id, Course course) {
    if (!courseRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Course", id);
    }

    var updatedCourse =
        Course.builder()
            .id(id)
            .ref(course.ref())
            .title(course.title())
            .creditCount(course.creditCount())
            .track(course.track())
            .semester(course.semester())
            .build();

    var saved = courseRepository.save(courseMapper.toEntity(updatedCourse));

    return courseMapper.toModel(saved);
  }

  public void delete(UUID id) {
    if (!courseRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Course", id);
    }

    courseRepository.deleteById(id);
  }
}

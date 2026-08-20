package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.Course;
import com.example.demo.repository.CourseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

  @Mock private CourseRepository courseRepository;
  @Mock private CourseMapper courseMapper;
  @InjectMocks private CourseService courseService;

  private Course buildCourse(UUID id) {
    return Course.builder()
        .id(id)
        .ref("PROG1")
        .title("Programmation 1")
        .creditCount(6)
        .track(Track.TRONC_COMMUN)
        .semester(Semester.S1)
        .build();
  }

  @Test
  void findAll() {
    var entity =
        com.example.demo.entity.JCourse.builder()
            .id(UUID.randomUUID())
            .ref("PROG1")
            .title("Programmation 1")
            .creditCount(6)
            .track(Track.TRONC_COMMUN)
            .semester(Semester.S1)
            .build();
    when(courseRepository.findAll()).thenReturn(List.of(entity));
    when(courseMapper.toModel(entity)).thenReturn(buildCourse(entity.getId()));

    var result = courseService.findAll();

    assertEquals(1, result.size());
    verify(courseRepository).findAll();
  }

  @Test
  void findById_found() {
    var id = UUID.randomUUID();
    var entity =
        com.example.demo.entity.JCourse.builder()
            .id(id)
            .ref("PROG1")
            .title("Programmation 1")
            .creditCount(6)
            .track(Track.TRONC_COMMUN)
            .semester(Semester.S1)
            .build();
    when(courseRepository.findById(id)).thenReturn(Optional.of(entity));
    when(courseMapper.toModel(entity)).thenReturn(buildCourse(id));

    var result = courseService.findById(id);

    assertEquals("PROG1", result.ref());
  }

  @Test
  void findById_notFound() {
    var id = UUID.randomUUID();
    when(courseRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> courseService.findById(id));
  }

  @Test
  void create() {
    var course = buildCourse(null);
    when(courseMapper.toEntity(any()))
        .thenReturn(
            com.example.demo.entity.JCourse.builder()
                .ref("PROG1")
                .title("Programmation 1")
                .creditCount(6)
                .track(Track.TRONC_COMMUN)
                .semester(Semester.S1)
                .build());
    var savedEntity =
        com.example.demo.entity.JCourse.builder()
            .id(UUID.randomUUID())
            .ref("PROG1")
            .title("Programmation 1")
            .creditCount(6)
            .track(Track.TRONC_COMMUN)
            .semester(Semester.S1)
            .build();
    when(courseRepository.save(any())).thenReturn(savedEntity);
    when(courseMapper.toModel(savedEntity)).thenReturn(buildCourse(savedEntity.getId()));

    var result = courseService.create(course);

    assertEquals("PROG1", result.ref());
    verify(courseRepository).save(any());
  }

  @Test
  void update() {
    var id = UUID.randomUUID();
    when(courseRepository.existsById(id)).thenReturn(true);
    when(courseMapper.toEntity(any()))
        .thenReturn(
            com.example.demo.entity.JCourse.builder()
                .id(id)
                .ref("PROG1")
                .title("Programmation 1")
                .creditCount(6)
                .track(Track.TRONC_COMMUN)
                .semester(Semester.S1)
                .build());
    var savedEntity =
        com.example.demo.entity.JCourse.builder()
            .id(id)
            .ref("PROG1")
            .title("Programmation 1")
            .creditCount(6)
            .track(Track.TRONC_COMMUN)
            .semester(Semester.S1)
            .build();
    when(courseRepository.save(any())).thenReturn(savedEntity);
    when(courseMapper.toModel(savedEntity)).thenReturn(buildCourse(id));

    var result = courseService.update(id, buildCourse(null));

    assertEquals(id, result.id());
  }

  @Test
  void update_notFound() {
    var id = UUID.randomUUID();
    when(courseRepository.existsById(id)).thenReturn(false);

    assertThrows(
        ResourceNotFoundException.class, () -> courseService.update(id, buildCourse(null)));
  }

  @Test
  void delete() {
    var id = UUID.randomUUID();
    when(courseRepository.existsById(id)).thenReturn(true);

    courseService.delete(id);

    verify(courseRepository).deleteById(id);
  }

  @Test
  void delete_notFound() {
    var id = UUID.randomUUID();
    when(courseRepository.existsById(id)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> courseService.delete(id));
  }
}

package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.entity.JCourse;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseMapperTest {

  private final CourseMapper courseMapper = new CourseMapper();

  @Test
  void toModelAndBack() {
    var id = UUID.randomUUID();
    var entity =
        JCourse.builder()
            .id(id)
            .ref("PROG1")
            .title("Programmation 1")
            .creditCount(6)
            .track(Track.TRONC_COMMUN)
            .semester(Semester.S1)
            .build();

    var model = courseMapper.toModel(entity);

    assertEquals(id, model.id());
    assertEquals("PROG1", model.ref());
    assertEquals("Programmation 1", model.title());
    assertEquals(6, model.creditCount());
    assertEquals(Track.TRONC_COMMUN, model.track());
    assertEquals(Semester.S1, model.semester());

    var backToEntity = courseMapper.toEntity(model);

    assertEquals(id, backToEntity.getId());
    assertEquals("PROG1", backToEntity.getRef());
  }
}

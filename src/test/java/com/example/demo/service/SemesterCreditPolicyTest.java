package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.entity.JCourse;
import com.example.demo.entity.JCourseOffering;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.exception.ConflictException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SemesterCreditPolicyTest {

  @InjectMocks private SemesterCreditPolicy semesterCreditPolicy;

  private JCourse buildCourse(Semester semester, int credits) {
    return JCourse.builder()
        .id(java.util.UUID.randomUUID())
        .ref("TEST")
        .title("Test")
        .creditCount(credits)
        .track(Track.TRONC_COMMUN)
        .semester(semester)
        .build();
  }

  @Test
  void checkCanAssign_withinLimit() {
    var existing = buildCourse(Semester.S1, 20);
    var offering = JCourseOffering.builder().course(existing).build();
    var newCourse = buildCourse(Semester.S1, 10);

    assertDoesNotThrow(() -> semesterCreditPolicy.checkCanAssign(List.of(offering), newCourse));
  }

  @Test
  void checkCanAssign_exceedsLimit() {
    var existing = buildCourse(Semester.S1, 25);
    var offering = JCourseOffering.builder().course(existing).build();
    var newCourse = buildCourse(Semester.S1, 10);

    assertThrows(
        ConflictException.class,
        () -> semesterCreditPolicy.checkCanAssign(List.of(offering), newCourse));
  }

  @Test
  void checkCanAssign_differentSemester() {
    var existing = buildCourse(Semester.S2, 25);
    var offering = JCourseOffering.builder().course(existing).build();
    var newCourse = buildCourse(Semester.S1, 10);

    assertDoesNotThrow(() -> semesterCreditPolicy.checkCanAssign(List.of(offering), newCourse));
  }

  @Test
  void checkCanAssign_emptyList() {
    var newCourse = buildCourse(Semester.S1, 30);

    assertDoesNotThrow(() -> semesterCreditPolicy.checkCanAssign(List.of(), newCourse));
  }
}

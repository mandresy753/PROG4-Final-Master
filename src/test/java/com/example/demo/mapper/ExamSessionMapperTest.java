package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.entity.*;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExamSessionMapperTest {

  private final ExamSessionMapper examSessionMapper =
      new ExamSessionMapper(
          new ExamMapper(
              new CourseOfferingMapper(
                  new CourseMapper(), new AcademicYearMapper(), new GroupMapper())),
          new UserMapper(),
          new GroupMapper());

  @Test
  void toModel_withTeacher() {
    var id = UUID.randomUUID();
    var teacherId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var yearId = UUID.randomUUID();

    var group = JGroup.builder().id(groupId).reference("K1").track(Track.TRONC_COMMUN).build();
    var course =
        JCourse.builder()
            .id(courseId)
            .ref("PROG1")
            .title("Programmation 1")
            .creditCount(6)
            .track(Track.TRONC_COMMUN)
            .semester(Semester.S1)
            .build();
    var year =
        JAcademicYear.builder()
            .id(yearId)
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 7, 31))
            .build();
    var offering =
        JCourseOffering.builder()
            .id(offeringId)
            .course(course)
            .academicYear(year)
            .groups(new HashSet<>(Set.of(group)))
            .build();
    var exam =
        JExam.builder().id(examId).courseOffering(offering).coefficient(BigDecimal.ONE).build();
    var teacher =
        JUser.builder()
            .id(teacherId)
            .reference("TCH001")
            .lastName("Dupont")
            .firstName("Jean")
            .email("jean@test.com")
            .password("hash")
            .role(UserRole.TEACHER)
            .build();
    var entity =
        JExamSession.builder()
            .id(id)
            .exam(exam)
            .examDate(LocalDateTime.of(2025, 12, 1, 9, 0))
            .teacher(teacher)
            .groups(new HashSet<>(Set.of(group)))
            .build();

    var model = examSessionMapper.toModel(entity);

    assertEquals(id, model.id());
    assertEquals(examId, model.exam().id());
    assertEquals(BigDecimal.ONE, model.exam().coefficient());
    assertEquals(LocalDateTime.of(2025, 12, 1, 9, 0), model.examDate());
    assertNotNull(model.teacher());
    assertEquals(teacherId, model.teacher().id());
    assertEquals(1, model.groups().size());
  }

  @Test
  void toModel_withoutTeacher() {
    var id = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var groupId = UUID.randomUUID();
    var offeringId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var yearId = UUID.randomUUID();

    var group = JGroup.builder().id(groupId).reference("K2").track(Track.EL).build();
    var course =
        JCourse.builder()
            .id(courseId)
            .ref("EL1")
            .title("Electronique 1")
            .creditCount(6)
            .track(Track.EL)
            .semester(Semester.S4)
            .build();
    var year =
        JAcademicYear.builder()
            .id(yearId)
            .label("2026-2027")
            .startDate(LocalDate.of(2026, 9, 1))
            .endDate(LocalDate.of(2027, 7, 31))
            .build();
    var offering =
        JCourseOffering.builder()
            .id(offeringId)
            .course(course)
            .academicYear(year)
            .groups(new HashSet<>(Set.of(group)))
            .build();
    var exam =
        JExam.builder()
            .id(examId)
            .courseOffering(offering)
            .coefficient(new BigDecimal("0.4"))
            .build();
    var entity =
        JExamSession.builder()
            .id(id)
            .exam(exam)
            .examDate(LocalDateTime.of(2027, 6, 1, 14, 0))
            .teacher(null)
            .groups(new HashSet<>())
            .build();

    var model = examSessionMapper.toModel(entity);

    assertEquals(id, model.id());
    assertEquals(examId, model.exam().id());
    assertEquals(new BigDecimal("0.4"), model.exam().coefficient());
    assertNull(model.teacher());
    assertTrue(model.groups().isEmpty());
  }
}

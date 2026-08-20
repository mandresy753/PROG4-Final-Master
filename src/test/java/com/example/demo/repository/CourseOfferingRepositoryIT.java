package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.conf.FacadeIT;
import com.example.demo.entity.JCourse;
import com.example.demo.entity.JCourseOffering;
import com.example.demo.entity.JGroup;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
class CourseOfferingRepositoryIT extends FacadeIT {

  @Autowired private CourseOfferingRepository courseOfferingRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private AcademicYearRepository academicYearRepository;
  @Autowired private GroupRepository groupRepository;

  @Test
  @Transactional
  void shouldFindByGroupIdAndAcademicYearId() {
    var year =
        academicYearRepository.save(
            com.example.demo.entity.JAcademicYear.builder()
                .label("2025-2026")
                .startDate(java.time.LocalDate.of(2025, 9, 1))
                .endDate(java.time.LocalDate.of(2026, 7, 31))
                .build());

    var group =
        groupRepository.save(JGroup.builder().reference("K1").track(Track.TRONC_COMMUN).build());

    var course =
        courseRepository.save(
            JCourse.builder()
                .ref("PROG1")
                .title("Programmation 1")
                .creditCount(6)
                .track(Track.TRONC_COMMUN)
                .semester(Semester.S1)
                .build());

    var offering =
        courseOfferingRepository.save(
            JCourseOffering.builder()
                .course(course)
                .academicYear(year)
                .groups(new HashSet<>(Set.of(group)))
                .build());

    var result =
        courseOfferingRepository.findByGroupIdAndAcademicYear_Id(group.getId(), year.getId());

    assertEquals(1, result.size());
    assertEquals(offering.getId(), result.get(0).getId());
  }

  @Test
  @Transactional
  void shouldFindByCourseIdAndAcademicYearIdAndGroupId() {
    var year =
        academicYearRepository.save(
            com.example.demo.entity.JAcademicYear.builder()
                .label("2026-2027")
                .startDate(java.time.LocalDate.of(2026, 9, 1))
                .endDate(java.time.LocalDate.of(2027, 7, 31))
                .build());

    var group =
        groupRepository.save(JGroup.builder().reference("K2").track(Track.TRONC_COMMUN).build());

    var course =
        courseRepository.save(
            JCourse.builder()
                .ref("WEB1")
                .title("Web 1")
                .creditCount(6)
                .track(Track.TRONC_COMMUN)
                .semester(Semester.S1)
                .build());

    courseOfferingRepository.save(
        JCourseOffering.builder()
            .course(course)
            .academicYear(year)
            .groups(new HashSet<>(Set.of(group)))
            .build());

    var result =
        courseOfferingRepository.findByCourse_IdAndAcademicYear_IdAndGroupId(
            course.getId(), year.getId(), group.getId());

    assertEquals(1, result.size());
  }

  @Test
  @Transactional
  void shouldFindByAcademicYearId() {
    var year =
        academicYearRepository.save(
            com.example.demo.entity.JAcademicYear.builder()
                .label("2027-2028")
                .startDate(java.time.LocalDate.of(2027, 9, 1))
                .endDate(java.time.LocalDate.of(2028, 7, 31))
                .build());

    var group =
        groupRepository.save(JGroup.builder().reference("K3").track(Track.TRONC_COMMUN).build());

    var course =
        courseRepository.save(
            JCourse.builder()
                .ref("BASE1")
                .title("Base de données")
                .creditCount(6)
                .track(Track.TRONC_COMMUN)
                .semester(Semester.S1)
                .build());

    courseOfferingRepository.save(
        JCourseOffering.builder()
            .course(course)
            .academicYear(year)
            .groups(new HashSet<>(Set.of(group)))
            .build());

    var result = courseOfferingRepository.findByAcademicYear_Id(year.getId());

    assertEquals(1, result.size());
  }
}

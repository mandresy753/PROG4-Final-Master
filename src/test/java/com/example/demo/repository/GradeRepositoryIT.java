package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.conf.FacadeIT;
import com.example.demo.entity.*;
import com.example.demo.enums.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
class GradeRepositoryIT extends FacadeIT {

  @Autowired private GradeRepository gradeRepository;
  @Autowired private ExamSessionRepository examSessionRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private CourseOfferingRepository courseOfferingRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private AcademicYearRepository academicYearRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private UserRepository userRepository;

  @Test
  @Transactional
  void shouldFindByExamSessionAndStudentOrderByEntryDateDesc() {
    var year =
        academicYearRepository.save(
            JAcademicYear.builder()
                .label("2030-2031")
                .startDate(LocalDate.of(2030, 9, 1))
                .endDate(LocalDate.of(2031, 7, 31))
                .build());
    var group =
        groupRepository.save(JGroup.builder().reference("K6").track(Track.TRONC_COMMUN).build());
    var course =
        courseRepository.save(
            JCourse.builder()
                .ref("TEST1")
                .title("Test Course")
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
    var exam =
        examRepository.save(
            JExam.builder().courseOffering(offering).coefficient(BigDecimal.ONE).build());
    var examSession =
        examSessionRepository.save(
            JExamSession.builder()
                .exam(exam)
                .examDate(LocalDateTime.of(2030, 12, 1, 9, 0))
                .groups(new HashSet<>(Set.of(group)))
                .build());
    var student =
        userRepository.save(
            JUser.builder()
                .reference("STD0200")
                .lastName("Test")
                .firstName("Student")
                .email("test200@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());
    var teacher =
        userRepository.save(
            JUser.builder()
                .reference("TCH0200")
                .lastName("Test")
                .firstName("Teacher")
                .email("teach200@test.com")
                .password("hash")
                .role(UserRole.TEACHER)
                .build());

    gradeRepository.save(
        JGrade.builder()
            .examSession(examSession)
            .student(student)
            .value(new BigDecimal("15"))
            .entryDate(LocalDateTime.of(2030, 12, 1, 10, 0))
            .enteredBy(teacher)
            .build());
    gradeRepository.save(
        JGrade.builder()
            .examSession(examSession)
            .student(student)
            .value(new BigDecimal("16"))
            .entryDate(LocalDateTime.of(2030, 12, 2, 10, 0))
            .enteredBy(teacher)
            .reason("Correction")
            .build());

    var result =
        gradeRepository.findByExamSession_IdAndStudent_IdOrderByEntryDateDesc(
            examSession.getId(), student.getId());

    assertEquals(2, result.size());
    assertEquals(new BigDecimal("16"), result.get(0).getValue());
    assertEquals(new BigDecimal("15"), result.get(1).getValue());
  }

  @Test
  @Transactional
  void shouldFindLatestByExamAndStudent() {
    var year =
        academicYearRepository.save(
            JAcademicYear.builder()
                .label("2031-2032")
                .startDate(LocalDate.of(2031, 9, 1))
                .endDate(LocalDate.of(2032, 7, 31))
                .build());
    var group =
        groupRepository.save(JGroup.builder().reference("K7").track(Track.TRONC_COMMUN).build());
    var course =
        courseRepository.save(
            JCourse.builder()
                .ref("TEST2")
                .title("Test Course 2")
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
    var exam =
        examRepository.save(
            JExam.builder().courseOffering(offering).coefficient(BigDecimal.ONE).build());
    var examSession =
        examSessionRepository.save(
            JExamSession.builder()
                .exam(exam)
                .examDate(LocalDateTime.of(2031, 12, 1, 9, 0))
                .groups(new HashSet<>(Set.of(group)))
                .build());
    var student =
        userRepository.save(
            JUser.builder()
                .reference("STD0300")
                .lastName("Test2")
                .firstName("Student2")
                .email("test300@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());
    var teacher =
        userRepository.save(
            JUser.builder()
                .reference("TCH0300")
                .lastName("Test2")
                .firstName("Teacher2")
                .email("teach300@test.com")
                .password("hash")
                .role(UserRole.TEACHER)
                .build());

    gradeRepository.save(
        JGrade.builder()
            .examSession(examSession)
            .student(student)
            .value(new BigDecimal("12"))
            .entryDate(LocalDateTime.of(2031, 12, 1, 10, 0))
            .enteredBy(teacher)
            .build());
    gradeRepository.save(
        JGrade.builder()
            .examSession(examSession)
            .student(student)
            .value(new BigDecimal("15"))
            .entryDate(LocalDateTime.of(2031, 12, 2, 10, 0))
            .enteredBy(teacher)
            .build());

    var latest = gradeRepository.findLatestByExamAndStudent(exam.getId(), student.getId());

    assertTrue(latest.isPresent());
    assertEquals(new BigDecimal("15"), latest.get().getValue());
  }

  @Test
  @Transactional
  void shouldFindByStudentId() {
    var year =
        academicYearRepository.save(
            JAcademicYear.builder()
                .label("2032-2033")
                .startDate(LocalDate.of(2032, 9, 1))
                .endDate(LocalDate.of(2033, 7, 31))
                .build());
    var group =
        groupRepository.save(JGroup.builder().reference("K8").track(Track.TRONC_COMMUN).build());
    var course =
        courseRepository.save(
            JCourse.builder()
                .ref("TEST3")
                .title("Test Course 3")
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
    var exam =
        examRepository.save(
            JExam.builder().courseOffering(offering).coefficient(BigDecimal.ONE).build());
    var examSession =
        examSessionRepository.save(
            JExamSession.builder()
                .exam(exam)
                .examDate(LocalDateTime.of(2032, 12, 1, 9, 0))
                .groups(new HashSet<>(Set.of(group)))
                .build());
    var student =
        userRepository.save(
            JUser.builder()
                .reference("STD0400")
                .lastName("Test3")
                .firstName("Student3")
                .email("test400@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());
    var teacher =
        userRepository.save(
            JUser.builder()
                .reference("TCH0400")
                .lastName("Test3")
                .firstName("Teacher3")
                .email("teach400@test.com")
                .password("hash")
                .role(UserRole.TEACHER)
                .build());

    gradeRepository.save(
        JGrade.builder()
            .examSession(examSession)
            .student(student)
            .value(new BigDecimal("14"))
            .entryDate(LocalDateTime.of(2032, 12, 1, 10, 0))
            .enteredBy(teacher)
            .build());

    var result = gradeRepository.findByStudent_Id(student.getId());

    assertEquals(1, result.size());
  }
}

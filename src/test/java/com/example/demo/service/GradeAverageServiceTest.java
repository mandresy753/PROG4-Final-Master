package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.Level;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseOfferingMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.AcademicYear;
import com.example.demo.model.Course;
import com.example.demo.model.CourseOffering;
import com.example.demo.model.User;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradeAverageServiceTest {

  @Mock private GradeRepository gradeRepository;
  @Mock private ExamRepository examRepository;
  @Mock private CourseOfferingRepository courseOfferingRepository;
  @Mock private EnrollmentRepository enrollmentRepository;
  @Mock private UserRepository userRepository;
  @Mock private CourseOfferingMapper courseOfferingMapper;
  @Mock private UserMapper userMapper;
  @Mock private EnrollmentService enrollmentService;
  @InjectMocks private GradeAverageService gradeAverageService;

  private UUID studentId;
  private UUID courseOfferingId;
  private UUID academicYearId;

  @BeforeEach
  void setUp() {
    studentId = UUID.randomUUID();
    courseOfferingId = UUID.randomUUID();
    academicYearId = UUID.randomUUID();
  }

  private com.example.demo.entity.JUser buildStudent() {
    return com.example.demo.entity.JUser.builder().id(studentId).role(UserRole.STUDENT).build();
  }

  private com.example.demo.entity.JCourseOffering buildOffering() {
    var course =
        com.example.demo.entity.JCourse.builder()
            .id(UUID.randomUUID())
            .ref("PROG1")
            .title("Programmation 1")
            .creditCount(6)
            .track(Track.TRONC_COMMUN)
            .semester(Semester.S1)
            .build();
    var year =
        com.example.demo.entity.JAcademicYear.builder()
            .id(academicYearId)
            .label("2025-2026")
            .build();
    return com.example.demo.entity.JCourseOffering.builder()
        .id(courseOfferingId)
        .course(course)
        .academicYear(year)
        .build();
  }

  @Test
  void courseAverage_withGrades() {
    var offering = buildOffering();
    when(courseOfferingRepository.findById(courseOfferingId)).thenReturn(Optional.of(offering));
    when(courseOfferingMapper.toModel(offering))
        .thenReturn(CourseOffering.builder().id(courseOfferingId).build());

    var exam =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .coefficient(new BigDecimal("0.4"))
            .courseOffering(offering)
            .build();
    var exam2 =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .coefficient(new BigDecimal("0.6"))
            .courseOffering(offering)
            .build();
    when(examRepository.findByCourseOffering_Id(courseOfferingId)).thenReturn(List.of(exam, exam2));

    var grade1 = com.example.demo.entity.JGrade.builder().value(new BigDecimal("14")).build();
    var grade2 = com.example.demo.entity.JGrade.builder().value(new BigDecimal("16")).build();
    when(gradeRepository.findLatestByExamAndStudent(exam.getId(), studentId))
        .thenReturn(Optional.of(grade1));
    when(gradeRepository.findLatestByExamAndStudent(exam2.getId(), studentId))
        .thenReturn(Optional.of(grade2));

    var result = gradeAverageService.courseAverage(studentId, courseOfferingId);

    assertNotNull(result.average());
    assertTrue(result.complete());
  }

  @Test
  void courseAverage_noGrades() {
    var offering = buildOffering();
    when(courseOfferingRepository.findById(courseOfferingId)).thenReturn(Optional.of(offering));
    when(courseOfferingMapper.toModel(offering))
        .thenReturn(CourseOffering.builder().id(courseOfferingId).build());

    var exam =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .coefficient(new BigDecimal("0.4"))
            .courseOffering(offering)
            .build();
    when(examRepository.findByCourseOffering_Id(courseOfferingId)).thenReturn(List.of(exam));
    when(gradeRepository.findLatestByExamAndStudent(any(), eq(studentId)))
        .thenReturn(Optional.empty());

    var result = gradeAverageService.courseAverage(studentId, courseOfferingId);

    assertNull(result.average());
    assertFalse(result.complete());
  }

  @Test
  void courseAverage_offeringNotFound() {
    when(courseOfferingRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> gradeAverageService.courseAverage(studentId, courseOfferingId));
  }

  @Test
  void yearAverage() {
    var student = buildStudent();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    var groupId = UUID.randomUUID();
    var enrollment =
        com.example.demo.entity.JEnrollment.builder()
            .student(student)
            .group(
                com.example.demo.entity.JGroup.builder()
                    .id(groupId)
                    .track(Track.TRONC_COMMUN)
                    .build())
            .academicYear(
                com.example.demo.entity.JAcademicYear.builder().id(academicYearId).build())
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.findByStudent_Id(studentId)).thenReturn(List.of(enrollment));
    when(enrollmentService.finalTrack(studentId)).thenReturn(Optional.of(Track.EL));

    var offering = buildOffering();
    when(courseOfferingRepository.findByGroupIdAndAcademicYear_Id(groupId, academicYearId))
        .thenReturn(List.of(offering));
    when(courseOfferingRepository.findById(courseOfferingId)).thenReturn(Optional.of(offering));
    when(courseOfferingMapper.toModel(offering))
        .thenReturn(
            CourseOffering.builder()
                .id(courseOfferingId)
                .course(
                    Course.builder()
                        .id(offering.getCourse().getId())
                        .ref("PROG1")
                        .title("Programmation 1")
                        .creditCount(6)
                        .track(Track.TRONC_COMMUN)
                        .semester(Semester.S1)
                        .build())
                .academicYear(AcademicYear.builder().id(academicYearId).label("2025-2026").build())
                .build());

    var exam =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .coefficient(BigDecimal.ONE)
            .courseOffering(offering)
            .build();
    when(examRepository.findByCourseOffering_Id(courseOfferingId)).thenReturn(List.of(exam));

    var grade = com.example.demo.entity.JGrade.builder().value(new BigDecimal("14")).build();
    when(gradeRepository.findLatestByExamAndStudent(any(), eq(studentId)))
        .thenReturn(Optional.of(grade));

    var result = gradeAverageService.yearAverage(studentId, academicYearId);

    assertNotNull(result);
    assertEquals(6, result.totalCredits());
  }

  @Test
  void yearAverage_noEnrollments() {
    when(userRepository.findById(studentId)).thenReturn(Optional.of(buildStudent()));
    when(enrollmentRepository.findByStudent_Id(studentId)).thenReturn(List.of());

    assertThrows(
        BadRequestException.class,
        () -> gradeAverageService.yearAverage(studentId, academicYearId));
  }

  @Test
  void overallAverage() {
    var student = buildStudent();
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userMapper.toModel(any()))
        .thenReturn(User.builder().id(studentId).role(UserRole.STUDENT).build());

    var groupId = UUID.randomUUID();
    var enrollment =
        com.example.demo.entity.JEnrollment.builder()
            .student(student)
            .group(
                com.example.demo.entity.JGroup.builder()
                    .id(groupId)
                    .track(Track.TRONC_COMMUN)
                    .build())
            .academicYear(
                com.example.demo.entity.JAcademicYear.builder()
                    .id(academicYearId)
                    .label("2025-2026")
                    .startDate(LocalDate.of(2025, 9, 1))
                    .build())
            .level(Level.L1)
            .startDate(LocalDate.of(2025, 9, 1))
            .build();
    when(enrollmentRepository.findByStudent_Id(studentId)).thenReturn(List.of(enrollment));
    when(enrollmentService.finalTrack(studentId)).thenReturn(Optional.of(Track.EL));

    var offering = buildOffering();
    when(courseOfferingRepository.findByGroupIdAndAcademicYear_Id(groupId, academicYearId))
        .thenReturn(List.of(offering));
    when(courseOfferingRepository.findById(courseOfferingId)).thenReturn(Optional.of(offering));
    when(courseOfferingMapper.toModel(offering))
        .thenReturn(
            CourseOffering.builder()
                .id(courseOfferingId)
                .course(
                    Course.builder()
                        .id(offering.getCourse().getId())
                        .ref("PROG1")
                        .title("Programmation 1")
                        .creditCount(6)
                        .track(Track.TRONC_COMMUN)
                        .semester(Semester.S1)
                        .build())
                .academicYear(AcademicYear.builder().id(academicYearId).label("2025-2026").build())
                .build());

    var exam =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .coefficient(BigDecimal.ONE)
            .courseOffering(offering)
            .build();
    when(examRepository.findByCourseOffering_Id(courseOfferingId)).thenReturn(List.of(exam));

    var grade = com.example.demo.entity.JGrade.builder().value(new BigDecimal("14")).build();
    when(gradeRepository.findLatestByExamAndStudent(any(), eq(studentId)))
        .thenReturn(Optional.of(grade));

    var result = gradeAverageService.overallAverage(studentId);

    assertNotNull(result);
    assertEquals(6, result.totalCredits());
  }

  @Test
  void overallAverage_studentNotFound() {
    when(userRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> gradeAverageService.overallAverage(studentId));
  }

  @Test
  void overallAverage_notAStudent() {
    when(userRepository.findById(studentId))
        .thenReturn(
            Optional.of(
                com.example.demo.entity.JUser.builder()
                    .id(studentId)
                    .role(UserRole.TEACHER)
                    .build()));
    assertThrows(BadRequestException.class, () -> gradeAverageService.overallAverage(studentId));
  }
}

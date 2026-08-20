package com.example.demo.controller;

import com.example.demo.conf.FacadeIT;
import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.entity.JAcademicYear;
import com.example.demo.entity.JCourse;
import com.example.demo.entity.JCourseOffering;
import com.example.demo.entity.JEnrollment;
import com.example.demo.entity.JExam;
import com.example.demo.entity.JExamSession;
import com.example.demo.entity.JGroup;
import com.example.demo.entity.JTeacherAssignment;
import com.example.demo.entity.JUser;
import com.example.demo.enums.Level;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.ExamSessionRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AppUserPrincipal;
import com.example.demo.security.JwtService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public abstract class ControllerIT extends FacadeIT {

  @Autowired protected TestRestTemplate restTemplate;
  @Autowired protected JwtService jwtService;
  @Autowired protected PasswordEncoder passwordEncoder;

  @Autowired protected UserRepository userRepository;
  @Autowired protected GroupRepository groupRepository;
  @Autowired protected CourseRepository courseRepository;
  @Autowired protected AcademicYearRepository academicYearRepository;
  @Autowired protected CourseOfferingRepository courseOfferingRepository;
  @Autowired protected EnrollmentRepository enrollmentRepository;
  @Autowired protected ExamRepository examRepository;
  @Autowired protected ExamSessionRepository examSessionRepository;
  @Autowired protected TeacherAssignmentRepository teacherAssignmentRepository;
  @Autowired protected GradeRepository gradeRepository;

  @MockBean protected EventProducer<?> eventProducer;

  protected JUser createUser(UserRole role) {
    return createUser(role, shortId());
  }

  protected JUser createUser(UserRole role, String seed) {
    return userRepository.save(
        JUser.builder()
            .reference(role.referencePrefix() + "-" + seed)
            .firstName("Prenom" + seed)
            .lastName("Nom" + seed)
            .email(seed + "@hei.test")
            .password(passwordEncoder.encode("password123"))
            .role(role)
            .build());
  }

  protected JGroup createGroup(Track track) {
    return groupRepository.save(
        JGroup.builder().reference("GRP-" + shortId()).track(track).build());
  }

  protected JCourse createCourse(Track track, Semester semester, int creditCount) {
    return courseRepository.save(
        JCourse.builder()
            .ref("REF-" + shortId())
            .title("Cours " + shortId())
            .creditCount(creditCount)
            .track(track)
            .semester(semester)
            .build());
  }

  protected JAcademicYear createAcademicYear() {
    return academicYearRepository.save(
        JAcademicYear.builder()
            .label("Annee-" + shortId())
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 7, 31))
            .build());
  }

  protected JCourseOffering createCourseOffering(
      JCourse course, JAcademicYear year, JGroup... groups) {
    var offering = JCourseOffering.builder().course(course).academicYear(year).build();
    for (JGroup group : groups) {
      offering.getGroups().add(group);
    }
    return courseOfferingRepository.save(offering);
  }

  protected JEnrollment createEnrollment(
      JUser student, JGroup group, JAcademicYear year, Level level, LocalDate start) {
    return createEnrollment(student, group, year, level, start, null);
  }

  protected JEnrollment createEnrollment(
      JUser student,
      JGroup group,
      JAcademicYear year,
      Level level,
      LocalDate start,
      LocalDate end) {
    return enrollmentRepository.save(
        JEnrollment.builder()
            .student(student)
            .group(group)
            .academicYear(year)
            .level(level)
            .startDate(start)
            .endDate(end)
            .build());
  }

  protected JExam createExam(JCourseOffering offering, BigDecimal coefficient) {
    return examRepository.save(
        JExam.builder().courseOffering(offering).coefficient(coefficient).build());
  }

  protected JExamSession createExamSession(JExam exam, JUser teacher, JGroup... groups) {
    var session =
        JExamSession.builder().exam(exam).examDate(LocalDateTime.now()).teacher(teacher).build();
    for (JGroup group : groups) {
      session.getGroups().add(group);
    }
    return examSessionRepository.save(session);
  }

  protected JTeacherAssignment assignTeacher(JCourseOffering offering, JUser teacher) {
    return teacherAssignmentRepository.save(
        JTeacherAssignment.builder().courseOffering(offering).teacher(teacher).build());
  }

  protected static String shortId() {
    return UUID.randomUUID().toString().substring(0, 8);
  }

  protected HttpHeaders authHeaders(JUser user) {
    var token = jwtService.generateToken(AppUserPrincipal.of(user));
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  protected <T> ResponseEntity<T> get(String path, HttpHeaders headers, Class<T> type) {
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(null, headers), type);
  }

  protected ResponseEntity<String> get(String path, HttpHeaders headers) {
    return get(path, headers, String.class);
  }

  protected <T> ResponseEntity<T> post(
      String path, Object body, HttpHeaders headers, Class<T> type) {
    return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), type);
  }

  protected ResponseEntity<String> post(String path, Object body, HttpHeaders headers) {
    return post(path, body, headers, String.class);
  }

  protected <T> ResponseEntity<T> put(
      String path, Object body, HttpHeaders headers, Class<T> type) {
    return restTemplate.exchange(path, HttpMethod.PUT, new HttpEntity<>(body, headers), type);
  }

  protected ResponseEntity<Void> delete(String path, HttpHeaders headers) {
    return restTemplate.exchange(
        path, HttpMethod.DELETE, new HttpEntity<>(null, headers), Void.class);
  }
}

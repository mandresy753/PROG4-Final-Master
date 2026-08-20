package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.conf.FacadeIT;
import com.example.demo.entity.JEnrollment;
import com.example.demo.entity.JGroup;
import com.example.demo.entity.JUser;
import com.example.demo.enums.Level;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
class EnrollmentRepositoryIT extends FacadeIT {

  @Autowired private EnrollmentRepository enrollmentRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private AcademicYearRepository academicYearRepository;

  @Test
  @Transactional
  void shouldFindDistinctAcademicYearLabels() {
    var year =
        academicYearRepository.save(
            com.example.demo.entity.JAcademicYear.builder()
                .label("2028-2029")
                .startDate(LocalDate.of(2028, 9, 1))
                .endDate(LocalDate.of(2029, 7, 31))
                .build());

    var group =
        groupRepository.save(JGroup.builder().reference("K4").track(Track.TRONC_COMMUN).build());

    var student =
        userRepository.save(
            JUser.builder()
                .reference("STD0099")
                .lastName("Test")
                .firstName("Student")
                .email("test99@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());

    enrollmentRepository.save(
        JEnrollment.builder()
            .student(student)
            .group(group)
            .academicYear(year)
            .level(Level.L1)
            .startDate(LocalDate.of(2028, 9, 1))
            .build());

    var labels =
        enrollmentRepository.findDistinctAcademicYear_LabelByLevelOrderByAcademicYear_LabelDesc(
            Level.L1);

    assertTrue(labels.contains("2028-2029"));
  }

  @Test
  @Transactional
  void shouldFindByStudentId() {
    var year =
        academicYearRepository.save(
            com.example.demo.entity.JAcademicYear.builder()
                .label("2029-2030")
                .startDate(LocalDate.of(2029, 9, 1))
                .endDate(LocalDate.of(2030, 7, 31))
                .build());

    var group =
        groupRepository.save(JGroup.builder().reference("K5").track(Track.TRONC_COMMUN).build());

    var student =
        userRepository.save(
            JUser.builder()
                .reference("STD0100")
                .lastName("Test2")
                .firstName("Student2")
                .email("test100@test.com")
                .password("hash")
                .role(UserRole.STUDENT)
                .build());

    enrollmentRepository.save(
        JEnrollment.builder()
            .student(student)
            .group(group)
            .academicYear(year)
            .level(Level.L1)
            .startDate(LocalDate.of(2029, 9, 1))
            .build());

    var result = enrollmentRepository.findByStudent_Id(student.getId());

    assertEquals(1, result.size());
  }
}

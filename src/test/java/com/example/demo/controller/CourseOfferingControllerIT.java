package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.dto.CreateCourseOfferingRequest;
import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.CourseOffering;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CourseOfferingControllerIT extends ControllerIT {

  @Test
  void create_asAdmin_attachesOfferingToMultipleGroups() {
    var admin = createUser(UserRole.ADMIN);
    var course = createCourse(Track.TRONC_COMMUN, Semester.S3, 4);
    var year = createAcademicYear();
    var k1 = createGroup(Track.EL);
    var k3 = createGroup(Track.TN);

    var request =
        new CreateCourseOfferingRequest(
            course.getId(), year.getId(), List.of(k1.getId(), k3.getId()));

    ResponseEntity<CourseOffering> response =
        post("/course-offerings", request, authHeaders(admin), CourseOffering.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().groups())
        .extracting(g -> g.id())
        .containsExactlyInAnyOrder(k1.getId(), k3.getId());

    var otherGroup = createGroup(Track.EL);
    ResponseEntity<CourseOffering[]> byGroup =
        get(
            "/course-offerings?academicYearId=" + year.getId() + "&groupId=" + otherGroup.getId(),
            authHeaders(admin),
            CourseOffering[].class);
    assertThat(byGroup.getBody()).isEmpty();
  }

  @Test
  void findByGroupAndYear_onlyReturnsOfferingsAssignedToThatGroup() {
    var admin = createUser(UserRole.ADMIN);
    var course = createCourse(Track.EL, Semester.S4, 3);
    var year = createAcademicYear();
    var elGroup = createGroup(Track.EL);
    createCourseOffering(course, year, elGroup);

    ResponseEntity<CourseOffering[]> response =
        get(
            "/course-offerings?academicYearId=" + year.getId() + "&groupId=" + elGroup.getId(),
            authHeaders(admin),
            CourseOffering[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(1);
    assertThat(response.getBody()[0].course().id()).isEqualTo(course.getId());
  }

  @Test
  void create_asTeacher_isForbidden() {
    var teacher = createUser(UserRole.TEACHER);
    var course = createCourse(Track.TN, Semester.S5, 4);
    var year = createAcademicYear();

    var request = new CreateCourseOfferingRequest(course.getId(), year.getId(), List.of());

    ResponseEntity<String> response = post("/course-offerings", request, authHeaders(teacher));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}

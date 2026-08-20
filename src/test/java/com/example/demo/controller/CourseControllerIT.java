package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Course;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CourseControllerIT extends ControllerIT {

  @Test
  void create_asAdmin_persistsCourseWithTrackAndCredits() {
    var admin = createUser(UserRole.ADMIN);
    var payload =
        Course.builder()
            .ref("PROG4-" + shortId())
            .title("Programmation avancee")
            .creditCount(5)
            .track(Track.TRONC_COMMUN)
            .semester(Semester.S4)
            .build();

    ResponseEntity<Course> response = post("/courses", payload, authHeaders(admin), Course.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().ref()).isEqualTo(payload.ref());
    assertThat(response.getBody().track()).isEqualTo(Track.TRONC_COMMUN);
    assertThat(response.getBody().creditCount()).isEqualTo(5);
  }

  @Test
  void create_asTeacher_isForbidden() {
    var teacher = createUser(UserRole.TEACHER);
    var payload =
        Course.builder()
            .ref("WEB1-" + shortId())
            .title("Developpement web")
            .creditCount(4)
            .track(Track.EL)
            .semester(Semester.S3)
            .build();

    ResponseEntity<String> response = post("/courses", payload, authHeaders(teacher));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void findAll_asStudent_isAllowedReadOnly() {
    createCourse(Track.EL, Semester.S4, 4);
    var student = createUser(UserRole.STUDENT);

    ResponseEntity<Course[]> response = get("/courses", authHeaders(student), Course[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void delete_asAdmin_removesCourse() {
    var admin = createUser(UserRole.ADMIN);
    var course = createCourse(Track.TN, Semester.S5, 3);

    ResponseEntity<Void> response = delete("/courses/" + course.getId(), authHeaders(admin));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(courseRepository.findById(course.getId())).isEmpty();
  }
}

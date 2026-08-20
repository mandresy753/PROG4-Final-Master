package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Group;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GroupControllerIT extends ControllerIT {

  @Test
  void create_asAdmin_persistsGroup() {
    var admin = createUser(UserRole.ADMIN);
    var payload = Group.builder().reference("K1-" + shortId()).track(Track.EL).build();

    ResponseEntity<Group> response = post("/groups", payload, authHeaders(admin), Group.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().track()).isEqualTo(Track.EL);
  }

  @Test
  void create_asStudent_isForbidden() {
    var student = createUser(UserRole.STUDENT);
    var payload = Group.builder().reference("K3-" + shortId()).track(Track.TN).build();

    ResponseEntity<String> response = post("/groups", payload, authHeaders(student));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void update_asAdmin_changesTrack() {
    var admin = createUser(UserRole.ADMIN);
    var group = createGroup(Track.EL);
    var payload =
        Group.builder().id(group.getId()).reference(group.getReference()).track(Track.TN).build();

    ResponseEntity<Group> response =
        put("/groups/" + group.getId(), payload, authHeaders(admin), Group.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().track()).isEqualTo(Track.TN);
    assertThat(groupRepository.findById(group.getId()).orElseThrow().getTrack())
        .isEqualTo(Track.TN);
  }
}

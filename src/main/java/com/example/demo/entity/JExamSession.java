package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exam_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JExamSession {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "exam_id", nullable = false)
  private JExam exam;

  @Column(nullable = false)
  private LocalDateTime examDate;

  @ManyToOne
  @JoinColumn(name = "teacher_id")
  private JUser teacher;

  @Builder.Default
  @ManyToMany
  @JoinTable(
      name = "exam_session_groups",
      joinColumns = @JoinColumn(name = "exam_session_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private Set<JGroup> groups = new HashSet<>();
}

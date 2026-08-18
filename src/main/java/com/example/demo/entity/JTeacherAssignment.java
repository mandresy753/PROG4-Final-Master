package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "teacher_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_offering_id", "teacher_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JTeacherAssignment {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "course_offering_id", nullable = false)
  private JCourseOffering courseOffering;

  @ManyToOne(optional = false)
  @JoinColumn(name = "teacher_id", nullable = false)
  private JUser teacher;
}

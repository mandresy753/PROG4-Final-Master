package com.example.demo.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "course_offerings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JCourseOffering {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private JCourse course;

  @ManyToOne(optional = false)
  @JoinColumn(name = "academic_year_id", nullable = false)
  private JAcademicYear academicYear;

  @Builder.Default
  @ManyToMany
  @JoinTable(
      name = "course_offering_groups",
      joinColumns = @JoinColumn(name = "course_offering_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  private Set<JGroup> groups = new HashSet<>();
}

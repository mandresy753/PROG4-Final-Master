package com.example.demo.entity;

import com.example.demo.enums.Level;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "enrollments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JEnrollment {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private JUser student;

  @ManyToOne(optional = false)
  @JoinColumn(name = "group_id", nullable = false)
  private JGroup group;

  @ManyToOne(optional = false)
  @JoinColumn(name = "academic_year_id", nullable = false)
  private JAcademicYear academicYear;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Level level;

  @Column(nullable = false)
  private LocalDate startDate;

  private LocalDate endDate;
}

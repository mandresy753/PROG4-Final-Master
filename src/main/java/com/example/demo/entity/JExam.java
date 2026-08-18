package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JExam {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "course_offering_id", nullable = false)
  private JCourseOffering courseOffering;

  @Column(nullable = false, precision = 4, scale = 3)
  private BigDecimal coefficient;
}

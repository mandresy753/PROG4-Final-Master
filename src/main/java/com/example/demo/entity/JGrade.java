package com.example.demo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JGrade {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "exam_session_id", nullable = false)
  private JExamSession examSession;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private JUser student;

  @Column(nullable = false, precision = 4, scale = 2)
  private BigDecimal value;

  @Column(nullable = false)
  private LocalDateTime entryDate;

  @ManyToOne(optional = false)
  @JoinColumn(name = "entered_by_id", nullable = false)
  private JUser enteredBy;

  private String reason;
}

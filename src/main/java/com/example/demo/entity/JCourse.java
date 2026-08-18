package com.example.demo.entity;

import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JCourse {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true)
  private String ref;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private Integer creditCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Track track;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Semester semester;
}

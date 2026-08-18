package com.example.demo.entity;

import com.example.demo.enums.Track;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JGroup {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true)
  private String reference;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Track track;
}

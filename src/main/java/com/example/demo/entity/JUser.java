package com.example.demo.entity;

import com.example.demo.enums.UserRole;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JUser {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true)
  private String reference;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;
}

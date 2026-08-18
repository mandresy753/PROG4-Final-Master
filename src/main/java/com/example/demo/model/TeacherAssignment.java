package com.example.demo.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record TeacherAssignment(UUID id, CourseOffering courseOffering, User teacher) {}

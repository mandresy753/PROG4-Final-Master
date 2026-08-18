package com.example.demo.model;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CourseOffering(
    UUID id, Course course, AcademicYear academicYear, List<Group> groups) {}

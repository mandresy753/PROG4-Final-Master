package com.example.demo.dto;

import java.util.List;
import java.util.UUID;

public record CreateCourseOfferingRequest(
    UUID courseId, UUID academicYearId, List<UUID> groupIds) {}

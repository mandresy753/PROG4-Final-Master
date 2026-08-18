package com.example.demo.model;

import com.example.demo.enums.Semester;
import com.example.demo.enums.Track;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Course(
    UUID id, String ref, String title, Integer creditCount, Track track, Semester semester) {}

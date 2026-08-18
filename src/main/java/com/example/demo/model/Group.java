package com.example.demo.model;

import com.example.demo.enums.Track;
import java.util.UUID;
import lombok.Builder;

@Builder
public record Group(UUID id, String reference, Track track) {}

package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.AcademicYearMapper;
import com.example.demo.model.AcademicYear;
import com.example.demo.repository.AcademicYearRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcademicYearServiceTest {

  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private AcademicYearMapper academicYearMapper;
  @InjectMocks private AcademicYearService academicYearService;

  private AcademicYear buildYear(UUID id) {
    return AcademicYear.builder()
        .id(id)
        .label("2025-2026")
        .startDate(LocalDate.of(2025, 9, 1))
        .endDate(LocalDate.of(2026, 7, 31))
        .build();
  }

  private com.example.demo.entity.JAcademicYear buildEntity(UUID id) {
    return com.example.demo.entity.JAcademicYear.builder()
        .id(id)
        .label("2025-2026")
        .startDate(LocalDate.of(2025, 9, 1))
        .endDate(LocalDate.of(2026, 7, 31))
        .build();
  }

  @Test
  void findAll() {
    var entity = buildEntity(UUID.randomUUID());
    when(academicYearRepository.findAll()).thenReturn(List.of(entity));
    when(academicYearMapper.toModel(entity)).thenReturn(buildYear(entity.getId()));

    assertEquals(1, academicYearService.findAll().size());
  }

  @Test
  void findById_found() {
    var id = UUID.randomUUID();
    when(academicYearRepository.findById(id)).thenReturn(Optional.of(buildEntity(id)));
    when(academicYearMapper.toModel(any())).thenReturn(buildYear(id));

    assertEquals("2025-2026", academicYearService.findById(id).label());
  }

  @Test
  void findById_notFound() {
    var id = UUID.randomUUID();
    when(academicYearRepository.findById(id)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> academicYearService.findById(id));
  }

  @Test
  void create() {
    when(academicYearMapper.toEntity(any())).thenReturn(buildEntity(null));
    var saved = buildEntity(UUID.randomUUID());
    when(academicYearRepository.save(any())).thenReturn(saved);
    when(academicYearMapper.toModel(saved)).thenReturn(buildYear(saved.getId()));

    assertNotNull(academicYearService.create(buildYear(null)));
  }

  @Test
  void update() {
    var id = UUID.randomUUID();
    when(academicYearRepository.existsById(id)).thenReturn(true);
    when(academicYearMapper.toEntity(any())).thenReturn(buildEntity(id));
    when(academicYearRepository.save(any())).thenReturn(buildEntity(id));
    when(academicYearMapper.toModel(any())).thenReturn(buildYear(id));

    assertEquals(id, academicYearService.update(id, buildYear(null)).id());
  }

  @Test
  void update_notFound() {
    var id = UUID.randomUUID();
    when(academicYearRepository.existsById(id)).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class, () -> academicYearService.update(id, buildYear(null)));
  }

  @Test
  void delete() {
    var id = UUID.randomUUID();
    when(academicYearRepository.existsById(id)).thenReturn(true);
    academicYearService.delete(id);
    verify(academicYearRepository).deleteById(id);
  }

  @Test
  void delete_notFound() {
    var id = UUID.randomUUID();
    when(academicYearRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> academicYearService.delete(id));
  }
}

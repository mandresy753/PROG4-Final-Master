package com.example.demo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.example.demo.entity.JAcademicYear;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AcademicYearMapperTest {

  private final AcademicYearMapper academicYearMapper = new AcademicYearMapper();

  @Test
  void toModelAndBack() {
    var id = UUID.randomUUID();
    var entity =
        JAcademicYear.builder()
            .id(id)
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 7, 31))
            .build();

    var model = academicYearMapper.toModel(entity);

    assertEquals(id, model.id());
    assertEquals("2025-2026", model.label());
    assertEquals(LocalDate.of(2025, 9, 1), model.startDate());

    var backToEntity = academicYearMapper.toEntity(model);

    assertEquals(id, backToEntity.getId());
    assertEquals("2025-2026", backToEntity.getLabel());
  }
}

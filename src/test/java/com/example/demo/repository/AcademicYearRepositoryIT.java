package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.conf.FacadeIT;
import com.example.demo.entity.JAcademicYear;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
class AcademicYearRepositoryIT extends FacadeIT {

  @Autowired private AcademicYearRepository repository;

  @Test
  @Transactional
  void shouldSaveAndFindAcademicYear() {
    JAcademicYear academicYear =
        JAcademicYear.builder()
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 7, 31))
            .build();

    JAcademicYear saved = repository.save(academicYear);

    assertNotNull(saved.getId());

    JAcademicYear found = repository.findById(saved.getId()).orElseThrow();

    assertEquals("2025-2026", found.getLabel());
    assertEquals(LocalDate.of(2025, 9, 1), found.getStartDate());
    assertEquals(LocalDate.of(2026, 7, 31), found.getEndDate());
  }
}

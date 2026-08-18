package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.AcademicYearMapper;
import com.example.demo.model.AcademicYear;
import com.example.demo.repository.AcademicYearRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AcademicYearService {

  private final AcademicYearRepository academicYearRepository;
  private final AcademicYearMapper academicYearMapper;

  public List<AcademicYear> findAll() {
    return academicYearRepository.findAll().stream().map(academicYearMapper::toModel).toList();
  }

  public AcademicYear findById(UUID id) {
    return academicYearRepository
        .findById(id)
        .map(academicYearMapper::toModel)
        .orElseThrow(() -> ResourceNotFoundException.of("Academic year", id));
  }

  public AcademicYear create(AcademicYear academicYear) {
    var entity = academicYearMapper.toEntity(academicYear);

    var saved = academicYearRepository.save(entity);

    return academicYearMapper.toModel(saved);
  }

  public AcademicYear update(UUID id, AcademicYear academicYear) {
    if (!academicYearRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Academic year", id);
    }

    var updatedAcademicYear =
        AcademicYear.builder()
            .id(id)
            .label(academicYear.label())
            .startDate(academicYear.startDate())
            .endDate(academicYear.endDate())
            .build();

    var saved = academicYearRepository.save(academicYearMapper.toEntity(updatedAcademicYear));
    return academicYearMapper.toModel(saved);
  }

  public void delete(UUID id) {
    if (!academicYearRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Academic year", id);
    }

    academicYearRepository.deleteById(id);
  }
}

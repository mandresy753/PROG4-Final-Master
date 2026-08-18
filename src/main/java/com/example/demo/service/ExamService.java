package com.example.demo.service;

import com.example.demo.entity.JExam;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ExamMapper;
import com.example.demo.model.Exam;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.ExamRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExamService {

  private static final BigDecimal TOTAL_COEFFICIENT = BigDecimal.ONE;

  private final ExamRepository examRepository;
  private final CourseOfferingRepository courseOfferingRepository;
  private final ExamMapper examMapper;

  public List<Exam> findByCourseOffering(UUID courseOfferingId) {
    if (!courseOfferingRepository.existsById(courseOfferingId)) {
      throw ResourceNotFoundException.of("Course offering", courseOfferingId);
    }

    return examRepository.findByCourseOffering_Id(courseOfferingId).stream()
        .map(examMapper::toModel)
        .toList();
  }

  public Exam findById(UUID id) {
    return examRepository
        .findById(id)
        .map(examMapper::toModel)
        .orElseThrow(() -> ResourceNotFoundException.of("Exam", id));
  }

  public Exam create(UUID courseOfferingId, BigDecimal coefficient) {

    if (!courseOfferingRepository.existsById(courseOfferingId)) {
      throw ResourceNotFoundException.of("Course offering", courseOfferingId);
    }

    validateCoefficient(coefficient);

    BigDecimal existingCoefficientSum =
        examRepository.findByCourseOffering_Id(courseOfferingId).stream()
            .map(JExam::getCoefficient)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal newCoefficientSum = existingCoefficientSum.add(coefficient);

    if (newCoefficientSum.compareTo(TOTAL_COEFFICIENT) > 0) {
      throw new BadRequestException(
          "The sum of exam coefficients for this course offering cannot exceed 1. "
              + "Current sum: "
              + existingCoefficientSum
              + ", new coefficient: "
              + coefficient
              + ", new sum: "
              + newCoefficientSum);
    }

    var entity =
        JExam.builder()
            .courseOffering(courseOfferingRepository.getReferenceById(courseOfferingId))
            .coefficient(coefficient)
            .build();

    return examMapper.toModel(examRepository.save(entity));
  }

  public boolean isComplete(UUID courseOfferingId) {
    if (!courseOfferingRepository.existsById(courseOfferingId)) {
      throw ResourceNotFoundException.of("Course offering", courseOfferingId);
    }

    BigDecimal totalCoefficient =
        examRepository.findByCourseOffering_Id(courseOfferingId).stream()
            .map(JExam::getCoefficient)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return totalCoefficient.compareTo(TOTAL_COEFFICIENT) == 0;
  }

  public BigDecimal getCoefficientSum(UUID courseOfferingId) {
    if (!courseOfferingRepository.existsById(courseOfferingId)) {
      throw ResourceNotFoundException.of("Course offering", courseOfferingId);
    }

    return examRepository.findByCourseOffering_Id(courseOfferingId).stream()
        .map(JExam::getCoefficient)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void validateCoefficient(BigDecimal coefficient) {
    if (coefficient == null) {
      throw new BadRequestException("The exam coefficient is required");
    }

    if (coefficient.compareTo(BigDecimal.ZERO) <= 0
        || coefficient.compareTo(TOTAL_COEFFICIENT) > 0) {
      throw new BadRequestException(
          "The exam coefficient must be strictly greater than 0 and at most 1");
    }
  }

  public void delete(UUID id) {
    if (!examRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Exam", id);
    }

    examRepository.deleteById(id);
  }
}

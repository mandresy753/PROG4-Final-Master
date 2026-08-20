package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ExamMapper;
import com.example.demo.model.Exam;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.ExamRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

  @Mock private ExamRepository examRepository;
  @Mock private CourseOfferingRepository courseOfferingRepository;
  @Mock private ExamMapper examMapper;
  @InjectMocks private ExamService examService;

  @Test
  void findByCourseOffering() {
    var offeringId = UUID.randomUUID();
    when(courseOfferingRepository.existsById(offeringId)).thenReturn(true);
    var entity =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .coefficient(new BigDecimal("0.4"))
            .build();
    when(examRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of(entity));
    when(examMapper.toModel(entity))
        .thenReturn(Exam.builder().id(entity.getId()).coefficient(new BigDecimal("0.4")).build());

    assertEquals(1, examService.findByCourseOffering(offeringId).size());
  }

  @Test
  void findByCourseOffering_notFound() {
    when(courseOfferingRepository.existsById(any())).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class, () -> examService.findByCourseOffering(UUID.randomUUID()));
  }

  @Test
  void findById_found() {
    var id = UUID.randomUUID();
    when(examRepository.findById(id))
        .thenReturn(
            Optional.of(
                com.example.demo.entity.JExam.builder()
                    .id(id)
                    .coefficient(new BigDecimal("0.4"))
                    .build()));
    when(examMapper.toModel(any()))
        .thenReturn(Exam.builder().id(id).coefficient(new BigDecimal("0.4")).build());

    assertNotNull(examService.findById(id));
  }

  @Test
  void findById_notFound() {
    when(examRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> examService.findById(UUID.randomUUID()));
  }

  @Test
  void create() {
    var offeringId = UUID.randomUUID();
    when(courseOfferingRepository.existsById(offeringId)).thenReturn(true);
    when(examRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of());
    var entity =
        com.example.demo.entity.JExam.builder()
            .id(UUID.randomUUID())
            .coefficient(new BigDecimal("0.4"))
            .build();
    when(examRepository.save(any())).thenReturn(entity);
    when(examMapper.toModel(any()))
        .thenReturn(Exam.builder().id(entity.getId()).coefficient(new BigDecimal("0.4")).build());

    assertNotNull(examService.create(offeringId, new BigDecimal("0.4")));
  }

  @Test
  void create_coefficientExceeds1() {
    var offeringId = UUID.randomUUID();
    when(courseOfferingRepository.existsById(offeringId)).thenReturn(true);
    var existing =
        com.example.demo.entity.JExam.builder().coefficient(new BigDecimal("0.8")).build();
    when(examRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of(existing));

    assertThrows(
        BadRequestException.class, () -> examService.create(offeringId, new BigDecimal("0.4")));
  }

  @Test
  void create_nullCoefficient() {
    when(courseOfferingRepository.existsById(any())).thenReturn(true);
    assertThrows(BadRequestException.class, () -> examService.create(UUID.randomUUID(), null));
  }

  @Test
  void create_zeroCoefficient() {
    when(courseOfferingRepository.existsById(any())).thenReturn(true);
    assertThrows(
        BadRequestException.class, () -> examService.create(UUID.randomUUID(), BigDecimal.ZERO));
  }

  @Test
  void create_negativeCoefficient() {
    when(courseOfferingRepository.existsById(any())).thenReturn(true);
    assertThrows(
        BadRequestException.class,
        () -> examService.create(UUID.randomUUID(), new BigDecimal("-1")));
  }

  @Test
  void create_offeringNotFound() {
    when(courseOfferingRepository.existsById(any())).thenReturn(false);
    assertThrows(
        ResourceNotFoundException.class,
        () -> examService.create(UUID.randomUUID(), new BigDecimal("0.4")));
  }

  @Test
  void isComplete_true() {
    var offeringId = UUID.randomUUID();
    when(courseOfferingRepository.existsById(offeringId)).thenReturn(true);
    var exam = com.example.demo.entity.JExam.builder().coefficient(BigDecimal.ONE).build();
    when(examRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of(exam));

    assertTrue(examService.isComplete(offeringId));
  }

  @Test
  void isComplete_false() {
    var offeringId = UUID.randomUUID();
    when(courseOfferingRepository.existsById(offeringId)).thenReturn(true);
    var exam = com.example.demo.entity.JExam.builder().coefficient(new BigDecimal("0.5")).build();
    when(examRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of(exam));

    assertFalse(examService.isComplete(offeringId));
  }

  @Test
  void isComplete_empty() {
    var offeringId = UUID.randomUUID();
    when(courseOfferingRepository.existsById(offeringId)).thenReturn(true);
    when(examRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of());

    assertFalse(examService.isComplete(offeringId));
  }

  @Test
  void getCoefficientSum() {
    var offeringId = UUID.randomUUID();
    when(courseOfferingRepository.existsById(offeringId)).thenReturn(true);
    var e1 = com.example.demo.entity.JExam.builder().coefficient(new BigDecimal("0.3")).build();
    var e2 = com.example.demo.entity.JExam.builder().coefficient(new BigDecimal("0.7")).build();
    when(examRepository.findByCourseOffering_Id(offeringId)).thenReturn(List.of(e1, e2));

    assertEquals(0, new BigDecimal("1.0").compareTo(examService.getCoefficientSum(offeringId)));
  }

  @Test
  void delete() {
    var id = UUID.randomUUID();
    when(examRepository.existsById(id)).thenReturn(true);
    examService.delete(id);
    verify(examRepository).deleteById(id);
  }

  @Test
  void delete_notFound() {
    var id = UUID.randomUUID();
    when(examRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> examService.delete(id));
  }
}

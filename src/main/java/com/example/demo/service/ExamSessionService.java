package com.example.demo.service;

import com.example.demo.entity.JExamSession;
import com.example.demo.entity.JGroup;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ExamSessionMapper;
import com.example.demo.model.ExamSession;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.ExamSessionRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExamSessionService {

  private final ExamSessionRepository examSessionRepository;
  private final ExamRepository examRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;
  private final ExamSessionMapper examSessionMapper;

  public List<ExamSession> findByExam(UUID examId) {
    if (!examRepository.existsById(examId)) {
      throw ResourceNotFoundException.of("Exam", examId);
    }

    return examSessionRepository.findByExam_Id(examId).stream()
        .map(examSessionMapper::toModel)
        .toList();
  }

  public ExamSession create(
      UUID examId, LocalDateTime examDate, UUID teacherId, List<UUID> groupIds) {

    var exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> ResourceNotFoundException.of("Exam", examId));

    if (groupIds == null || groupIds.isEmpty()) {
      throw new BadRequestException("At least one group is required for an exam session");
    }

    Set<UUID> distinctGroupIds = new HashSet<>(groupIds);
    List<JGroup> groups = groupRepository.findAllById(distinctGroupIds);

    if (groups.size() != distinctGroupIds.size()) {
      throw ResourceNotFoundException.of("Group", groupIds.get(0));
    }

    var offeringGroupIds =
        exam.getCourseOffering().getGroups().stream()
            .map(JGroup::getId)
            .collect(Collectors.toSet());

    if (!offeringGroupIds.containsAll(distinctGroupIds)) {
      throw new BadRequestException(
          "All groups of an exam session must belong to the exam's course offering");
    }

    var alreadyCoveredGroupIds =
        examSessionRepository.findByExam_Id(examId).stream()
            .flatMap(session -> session.getGroups().stream())
            .map(JGroup::getId)
            .collect(Collectors.toSet());

    if (distinctGroupIds.stream().anyMatch(alreadyCoveredGroupIds::contains)) {
      throw new BadRequestException("One of these groups already has a session for this exam");
    }

    var entity =
        JExamSession.builder()
            .exam(exam)
            .examDate(examDate)
            .teacher(teacherId == null ? null : userRepository.getReferenceById(teacherId))
            .groups(new HashSet<>(groups))
            .build();

    return examSessionMapper.toModel(examSessionRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!examSessionRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Exam session", id);
    }

    examSessionRepository.deleteById(id);
  }
}

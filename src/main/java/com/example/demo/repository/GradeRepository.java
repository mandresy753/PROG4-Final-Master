package com.example.demo.repository;

import com.example.demo.entity.JGrade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<JGrade, UUID> {

  List<JGrade> findByStudent_Id(UUID studentId);

  List<JGrade> findByExamSession_Id(UUID examSessionId);

  List<JGrade> findByExamSession_IdAndStudent_IdOrderByEntryDateDesc(
      UUID examSessionId, UUID studentId);

  List<JGrade> findByExamSession_Exam_IdAndStudent_IdOrderByEntryDateDesc(
      UUID examId, UUID studentId);

  default Optional<JGrade> findLatestByExamSessionAndStudent(UUID examSessionId, UUID studentId) {
    return findByExamSession_IdAndStudent_IdOrderByEntryDateDesc(examSessionId, studentId).stream()
        .findFirst();
  }

  default Optional<JGrade> findLatestByExamAndStudent(UUID examId, UUID studentId) {
    return findByExamSession_Exam_IdAndStudent_IdOrderByEntryDateDesc(examId, studentId).stream()
        .findFirst();
  }
}

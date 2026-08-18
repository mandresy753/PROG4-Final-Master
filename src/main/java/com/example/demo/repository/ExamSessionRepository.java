package com.example.demo.repository;

import com.example.demo.entity.JExamSession;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSessionRepository extends JpaRepository<JExamSession, UUID> {

  List<JExamSession> findByExam_Id(UUID examId);

  List<JExamSession> findByExam_CourseOffering_Id(UUID courseOfferingId);
}

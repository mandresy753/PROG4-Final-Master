package com.example.demo.repository;

import com.example.demo.entity.JTeacherAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherAssignmentRepository extends JpaRepository<JTeacherAssignment, UUID> {

  List<JTeacherAssignment> findByTeacher_Id(UUID teacherId);

  List<JTeacherAssignment> findByCourseOffering_Id(UUID courseOfferingId);

  List<JTeacherAssignment> findByCourseOffering_Course_IdAndCourseOffering_AcademicYear_Id(
      UUID courseId, UUID academicYearId);
}

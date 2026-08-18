package com.example.demo.repository;

import com.example.demo.entity.JEnrollment;
import com.example.demo.enums.Level;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<JEnrollment, UUID> {

  List<JEnrollment> findByStudent_Id(UUID studentId);

  Optional<JEnrollment> findFirstByStudent_IdAndLevelOrderByStartDateAsc(
      UUID studentId, Level level);

  List<JEnrollment> findByStudent_IdOrderByStartDateAsc(UUID studentId);

  List<JEnrollment> findByGroup_IdAndAcademicYear_Id(UUID groupId, UUID academicYearId);

  List<JEnrollment> findByAcademicYear_Id(UUID academicYearId);

  @Query(
      """
      SELECT DISTINCT e.academicYear.label
      FROM JEnrollment e
      WHERE e.level = :level
      ORDER BY e.academicYear.label DESC
      """)
  List<String> findDistinctAcademicYear_LabelByLevelOrderByAcademicYear_LabelDesc(
      @Param("level") Level level);
}

package com.example.demo.repository;

import com.example.demo.entity.JCourseOffering;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseOfferingRepository extends JpaRepository<JCourseOffering, UUID> {

  List<JCourseOffering> findByAcademicYear_Id(UUID academicYearId);

  List<JCourseOffering> findByCourse_Id(UUID courseId);

  @Query(
      "select co from JCourseOffering co join co.groups g "
          + "where g.id = :groupId and co.academicYear.id = :academicYearId")
  List<JCourseOffering> findByGroupIdAndAcademicYear_Id(
      @Param("groupId") UUID groupId, @Param("academicYearId") UUID academicYearId);

  @Query(
      "select co from JCourseOffering co join co.groups g where co.course.id = :courseId and"
          + " co.academicYear.id = :academicYearId and g.id = :groupId")
  List<JCourseOffering> findByCourse_IdAndAcademicYear_IdAndGroupId(
      @Param("courseId") UUID courseId,
      @Param("academicYearId") UUID academicYearId,
      @Param("groupId") UUID groupId);
}

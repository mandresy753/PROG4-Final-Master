package com.example.demo.service;

import com.example.demo.entity.JCourseOffering;
import com.example.demo.entity.JGroup;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseOfferingMapper;
import com.example.demo.model.CourseOffering;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.GroupRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseOfferingService {

  private final CourseOfferingRepository courseOfferingRepository;
  private final CourseRepository courseRepository;
  private final AcademicYearRepository academicYearRepository;
  private final GroupRepository groupRepository;
  private final CourseOfferingMapper courseOfferingMapper;
  private final SemesterCreditPolicy semesterCreditPolicy;

  public List<CourseOffering> findAll() {
    return courseOfferingRepository.findAll().stream().map(courseOfferingMapper::toModel).toList();
  }

  public List<CourseOffering> findByAcademicYear(UUID academicYearId) {
    return courseOfferingRepository.findByAcademicYear_Id(academicYearId).stream()
        .map(courseOfferingMapper::toModel)
        .toList();
  }

  public List<CourseOffering> findByGroupAndAcademicYear(UUID groupId, UUID academicYearId) {
    return courseOfferingRepository
        .findByGroupIdAndAcademicYear_Id(groupId, academicYearId)
        .stream()
        .map(courseOfferingMapper::toModel)
        .toList();
  }

  public CourseOffering findById(UUID id) {
    return courseOfferingRepository
        .findById(id)
        .map(courseOfferingMapper::toModel)
        .orElseThrow(() -> ResourceNotFoundException.of("Course offering", id));
  }

  public CourseOffering create(UUID courseId, UUID academicYearId, List<UUID> groupIds) {
    var course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course", courseId));

    if (!academicYearRepository.existsById(academicYearId)) {
      throw ResourceNotFoundException.of("Academic year", academicYearId);
    }

    if (groupIds == null || groupIds.isEmpty()) {
      throw new BadRequestException("At least one group is required");
    }

    Set<UUID> distinctGroupIds = new HashSet<>(groupIds);
    List<JGroup> groups = groupRepository.findAllById(distinctGroupIds);

    if (groups.size() != distinctGroupIds.size()) {
      throw ResourceNotFoundException.of("Group", groupIds.get(0));
    }

    for (UUID groupId : distinctGroupIds) {
      var alreadyAssigned =
          courseOfferingRepository.findByCourse_IdAndAcademicYear_IdAndGroupId(
              courseId, academicYearId, groupId);
      if (!alreadyAssigned.isEmpty()) {
        throw new ConflictException(
            "This course is already assigned to group "
                + groupId
                + " for this academic year (via another offering)");
      }
    }

    var existingOfferingsForCredits =
        distinctGroupIds.stream()
            .flatMap(
                groupId ->
                    courseOfferingRepository
                        .findByGroupIdAndAcademicYear_Id(groupId, academicYearId)
                        .stream())
            .distinct()
            .toList();

    semesterCreditPolicy.checkCanAssign(existingOfferingsForCredits, course);

    var entity =
        JCourseOffering.builder()
            .course(course)
            .academicYear(academicYearRepository.getReferenceById(academicYearId))
            .groups(new HashSet<>(groups))
            .build();

    return courseOfferingMapper.toModel(courseOfferingRepository.save(entity));
  }

  public void delete(UUID id) {
    if (!courseOfferingRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Course offering", id);
    }

    courseOfferingRepository.deleteById(id);
  }
}

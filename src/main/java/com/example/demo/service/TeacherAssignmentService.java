package com.example.demo.service;

import com.example.demo.entity.JTeacherAssignment;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.TeacherAssignmentMapper;
import com.example.demo.model.TeacherAssignment;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.TeacherAssignmentRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TeacherAssignmentService {

  private final TeacherAssignmentRepository teacherAssignmentRepository;
  private final CourseOfferingRepository courseOfferingRepository;
  private final UserRepository userRepository;
  private final TeacherAssignmentMapper teacherAssignmentMapper;

  public List<TeacherAssignment> findByTeacher(UUID teacherId) {
    return teacherAssignmentRepository.findByTeacher_Id(teacherId).stream()
        .map(teacherAssignmentMapper::toModel)
        .toList();
  }

  public List<TeacherAssignment> findByCourseOffering(UUID courseOfferingId) {
    return teacherAssignmentRepository.findByCourseOffering_Id(courseOfferingId).stream()
        .map(teacherAssignmentMapper::toModel)
        .toList();
  }

  public TeacherAssignment assign(UUID courseOfferingId, UUID teacherId) {
    if (!courseOfferingRepository.existsById(courseOfferingId)) {
      throw ResourceNotFoundException.of("Course offering", courseOfferingId);
    }

    var teacher =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> ResourceNotFoundException.of("Teacher", teacherId));

    if (teacher.getRole() != UserRole.TEACHER) {
      throw new BadRequestException("This user is not a teacher");
    }

    var entity =
        JTeacherAssignment.builder()
            .courseOffering(courseOfferingRepository.getReferenceById(courseOfferingId))
            .teacher(userRepository.getReferenceById(teacherId))
            .build();

    return teacherAssignmentMapper.toModel(teacherAssignmentRepository.save(entity));
  }

  public void unassign(UUID id) {
    if (!teacherAssignmentRepository.existsById(id)) {
      throw ResourceNotFoundException.of("Teacher assignment", id);
    }

    teacherAssignmentRepository.deleteById(id);
  }
}

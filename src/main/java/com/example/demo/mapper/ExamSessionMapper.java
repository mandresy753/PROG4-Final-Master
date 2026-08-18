package com.example.demo.mapper;

import com.example.demo.entity.JExamSession;
import com.example.demo.model.ExamSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExamSessionMapper {

  private final ExamMapper examMapper;
  private final UserMapper userMapper;
  private final GroupMapper groupMapper;

  public ExamSession toModel(JExamSession entity) {
    return ExamSession.builder()
        .id(entity.getId())
        .exam(examMapper.toModel(entity.getExam()))
        .examDate(entity.getExamDate())
        .teacher(entity.getTeacher() == null ? null : userMapper.toModel(entity.getTeacher()))
        .groups(entity.getGroups().stream().map(groupMapper::toModel).toList())
        .build();
  }
}

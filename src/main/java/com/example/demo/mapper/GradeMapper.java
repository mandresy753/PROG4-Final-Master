package com.example.demo.mapper;

import com.example.demo.entity.JGrade;
import com.example.demo.model.Grade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GradeMapper {

  private final ExamSessionMapper examSessionMapper;
  private final UserMapper userMapper;

  public Grade toModel(JGrade entity) {
    return Grade.builder()
        .id(entity.getId())
        .examSession(examSessionMapper.toModel(entity.getExamSession()))
        .student(userMapper.toModel(entity.getStudent()))
        .value(entity.getValue())
        .entryDate(entity.getEntryDate())
        .enteredBy(userMapper.toModel(entity.getEnteredBy()))
        .reason(entity.getReason())
        .build();
  }
}

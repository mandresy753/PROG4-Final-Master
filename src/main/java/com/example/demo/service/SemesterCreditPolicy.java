package com.example.demo.service;

import com.example.demo.entity.JCourse;
import com.example.demo.entity.JCourseOffering;
import com.example.demo.exception.ConflictException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SemesterCreditPolicy {

  public static final int MAX_CREDITS_PER_SEMESTER = 30;

  public void checkCanAssign(
      List<JCourseOffering> existingOfferingsForGroupAndYear, JCourse courseToAssign) {

    var creditsAlreadyAssigned =
        existingOfferingsForGroupAndYear.stream()
            .map(JCourseOffering::getCourse)
            .filter(course -> course.getSemester() == courseToAssign.getSemester())
            .mapToInt(JCourse::getCreditCount)
            .sum();

    var projectedTotal = creditsAlreadyAssigned + courseToAssign.getCreditCount();

    if (projectedTotal > MAX_CREDITS_PER_SEMESTER) {
      throw new ConflictException(
          "Assigning '%s' (%d credits) would bring %s to %d credits for this group/year, exceeding the %d-credit cap"
              .formatted(
                  courseToAssign.getRef(),
                  courseToAssign.getCreditCount(),
                  courseToAssign.getSemester(),
                  projectedTotal,
                  MAX_CREDITS_PER_SEMESTER));
    }
  }
}

package com.example.demo.service;

import com.example.demo.entity.JAcademicYear;
import com.example.demo.entity.JCourseOffering;
import com.example.demo.entity.JExam;
import com.example.demo.entity.JUser;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CourseOfferingMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.report.CourseAverage;
import com.example.demo.model.report.OverallAverage;
import com.example.demo.model.report.YearAverage;
import com.example.demo.repository.CourseOfferingRepository;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GradeAverageService {

  private static final int SCALE = 2;

  private final GradeRepository gradeRepository;
  private final ExamRepository examRepository;
  private final CourseOfferingRepository courseOfferingRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final UserRepository userRepository;
  private final CourseOfferingMapper courseOfferingMapper;
  private final UserMapper userMapper;
  private final EnrollmentService enrollmentService;

  public CourseAverage courseAverage(UUID studentId, UUID courseOfferingId) {
    var courseOffering =
        courseOfferingRepository
            .findById(courseOfferingId)
            .orElseThrow(() -> ResourceNotFoundException.of("Course offering", courseOfferingId));

    var exams = examRepository.findByCourseOffering_Id(courseOfferingId);

    var totalCoefficient =
        exams.stream().map(JExam::getCoefficient).reduce(BigDecimal.ZERO, BigDecimal::add);

    var weightedSum = BigDecimal.ZERO;
    var gradedCoefficientSum = BigDecimal.ZERO;

    for (JExam exam : exams) {
      var latestGrade = gradeRepository.findLatestByExamAndStudent(exam.getId(), studentId);

      if (latestGrade.isPresent()) {
        weightedSum = weightedSum.add(latestGrade.get().getValue().multiply(exam.getCoefficient()));
        gradedCoefficientSum = gradedCoefficientSum.add(exam.getCoefficient());
      }
    }

    var average =
        gradedCoefficientSum.compareTo(BigDecimal.ZERO) == 0
            ? null
            : weightedSum.divide(gradedCoefficientSum, SCALE, RoundingMode.HALF_UP);

    var complete =
        !exams.isEmpty()
            && gradedCoefficientSum.compareTo(totalCoefficient) == 0
            && totalCoefficient.compareTo(BigDecimal.ONE) == 0;

    return CourseAverage.builder()
        .courseOffering(courseOfferingMapper.toModel(courseOffering))
        .average(average)
        .complete(complete)
        .gradedCoefficientSum(gradedCoefficientSum)
        .build();
  }

  public YearAverage yearAverage(UUID studentId, UUID academicYearId) {
    var student = requireStudent(studentId);

    var groupIdsForYear =
        enrollmentRepository.findByStudent_Id(studentId).stream()
            .filter(e -> e.getAcademicYear().getId().equals(academicYearId))
            .map(e -> e.getGroup().getId())
            .distinct()
            .toList();

    if (groupIdsForYear.isEmpty()) {
      throw new BadRequestException(
          "This student was not enrolled in this academic year: " + academicYearId);
    }

    var studentTrack = enrollmentService.finalTrack(studentId).orElse(null);

    var courseOfferingIds =
        groupIdsForYear.stream()
            .flatMap(
                groupId ->
                    courseOfferingRepository
                        .findByGroupIdAndAcademicYear_Id(groupId, academicYearId)
                        .stream())
            .filter(offering -> matchesTrack(offering, studentTrack))
            .map(offering -> offering.getId())
            .distinct()
            .toList();

    var courseAverages =
        courseOfferingIds.stream().map(id -> courseAverage(studentId, id)).toList();

    return buildYearAverage(courseAverages);
  }

  private boolean matchesTrack(JCourseOffering offering, Track studentTrack) {
    var courseTrack = offering.getCourse().getTrack();
    return courseTrack == Track.TRONC_COMMUN || courseTrack == studentTrack;
  }

  private YearAverage buildYearAverage(List<CourseAverage> courseAverages) {
    var weightedSum = BigDecimal.ZERO;
    var creditsGraded = 0;
    var totalCredits = 0;
    var validatedCredits = 0;
    var complete = !courseAverages.isEmpty();

    for (CourseAverage courseAverage : courseAverages) {
      var credits = courseAverage.courseOffering().course().creditCount();
      totalCredits += credits;

      if (!courseAverage.complete()) {
        complete = false;
      }

      if (courseAverage.average() != null) {
        weightedSum =
            weightedSum.add(courseAverage.average().multiply(BigDecimal.valueOf(credits)));
        creditsGraded += credits;

        if (courseAverage.average().compareTo(BigDecimal.TEN) >= 0) {
          validatedCredits += credits;
        }
      }
    }

    var academicYear =
        courseAverages.isEmpty() ? null : courseAverages.get(0).courseOffering().academicYear();

    var generalAverage =
        creditsGraded == 0
            ? null
            : weightedSum.divide(BigDecimal.valueOf(creditsGraded), SCALE, RoundingMode.HALF_UP);

    return YearAverage.builder()
        .academicYear(academicYear)
        .courseAverages(courseAverages)
        .generalAverage(generalAverage)
        .totalCredits(totalCredits)
        .validatedCredits(validatedCredits)
        .complete(complete)
        .build();
  }

  public OverallAverage overallAverage(UUID studentId) {
    var student = requireStudent(studentId);

    var academicYears =
        enrollmentRepository.findByStudent_Id(studentId).stream()
            .collect(
                Collectors.toMap(
                    e -> e.getAcademicYear().getId(), e -> e.getAcademicYear(), (a, b) -> a))
            .values()
            .stream()
            .sorted(Comparator.comparing(JAcademicYear::getStartDate))
            .toList();

    var years = academicYears.stream().map(year -> yearAverage(studentId, year.getId())).toList();

    var weightedSum = BigDecimal.ZERO;
    var creditsGraded = 0;
    var totalCredits = 0;
    var complete = !years.isEmpty();

    for (YearAverage year : years) {
      totalCredits += year.totalCredits();

      if (!year.complete()) {
        complete = false;
      }

      if (year.generalAverage() != null) {
        weightedSum =
            weightedSum.add(
                year.generalAverage().multiply(BigDecimal.valueOf(year.totalCredits())));
        creditsGraded += year.totalCredits();
      }
    }

    var overallAverage =
        creditsGraded == 0
            ? null
            : weightedSum.divide(BigDecimal.valueOf(creditsGraded), SCALE, RoundingMode.HALF_UP);

    return OverallAverage.builder()
        .student(userMapper.toModel(student))
        .years(years)
        .overallAverage(overallAverage)
        .totalCredits(totalCredits)
        .complete(complete)
        .build();
  }

  private JUser requireStudent(UUID studentId) {
    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));

    if (student.getRole() != UserRole.STUDENT) {
      throw new BadRequestException("This user is not a student");
    }

    return student;
  }
}

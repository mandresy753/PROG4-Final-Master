package com.example.demo.service;

import com.example.demo.enums.Level;
import com.example.demo.enums.Track;
import com.example.demo.enums.UserRole;
import com.example.demo.model.Graduate;
import com.example.demo.model.User;
import com.example.demo.repository.EnrollmentRepository;
import com.example.demo.repository.GraduateRankingRow;
import com.example.demo.repository.GraduationQueryRepository;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GraduationService {

  public static final int EXPECTED_TOTAL_CREDITS =
      SemesterCreditPolicy.MAX_CREDITS_PER_SEMESTER * 6;

  private static final List<Track> GRADUATION_TRACKS = List.of(Track.EL, Track.TN);

  private final GraduationQueryRepository graduationQueryRepository;
  private final EnrollmentRepository enrollmentRepository;

  public List<String> listPromotions() {
    return enrollmentRepository.findDistinctAcademicYear_LabelByLevelOrderByAcademicYear_LabelDesc(
        Level.L1);
  }

  public List<Graduate> listGraduates(Track track, String promotion) {
    return graduationQueryRepository
        .findRankedGraduates(track.name(), promotion, EXPECTED_TOTAL_CREDITS)
        .stream()
        .map(row -> toGraduate(row, track, promotion))
        .toList();
  }

  public Map<Track, List<Graduate>> listGraduatesByPromotion(String promotion) {
    var byTrack = new EnumMap<Track, List<Graduate>>(Track.class);
    for (Track track : GRADUATION_TRACKS) {
      byTrack.put(track, listGraduates(track, promotion));
    }
    return byTrack;
  }

  private Graduate toGraduate(GraduateRankingRow row, Track track, String promotion) {
    var student =
        User.builder()
            .id(row.getId())
            .reference(row.getReference())
            .lastName(row.getLastName())
            .firstName(row.getFirstName())
            .email(row.getEmail())
            .role(UserRole.STUDENT)
            .build();

    return Graduate.builder()
        .student(student)
        .track(track)
        .promotion(promotion)
        .overallAverage(row.getOverallAverage())
        .totalCredits(row.getTotalCredits())
        .graduated(true)
        .rank(row.getRank())
        .build();
  }
}

package com.example.demo.service;

import com.example.demo.file.bucket.BucketComponent;
import java.time.Duration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GraduateExportService {

  private static final String BUCKET_PREFIX = "graduates/";
  private static final Duration DOWNLOAD_LINK_DURATION = Duration.ofMinutes(15);

  private final GraduationService graduationService;
  private final GraduateXlsxGenerator graduateXlsxGenerator;
  private final BucketComponent bucketComponent;

  public String exportToXlsx(String promotion) {
    var graduatesByTrack = graduationService.listGraduatesByPromotion(promotion);
    var file = graduateXlsxGenerator.generate(graduatesByTrack);

    var bucketKey = BUCKET_PREFIX + promotion + ".xlsx";
    bucketComponent.upload(file, bucketKey);

    return bucketComponent.presign(bucketKey, DOWNLOAD_LINK_DURATION).toString();
  }
}

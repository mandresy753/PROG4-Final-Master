package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.enums.Track;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.model.Graduate;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduateExportServiceTest {

  @Mock private GraduationService graduationService;
  @Mock private GraduateXlsxGenerator graduateXlsxGenerator;
  @Mock private BucketComponent bucketComponent;
  @InjectMocks private GraduateExportService graduateExportService;

  @SneakyThrows
  @Test
  void exportToXlsx() {
    var graduatesByTrack =
        Map.of(
            Track.EL, List.<Graduate>of(),
            Track.TN, List.<Graduate>of());
    when(graduationService.listGraduatesByPromotion("2025-2026")).thenReturn(graduatesByTrack);

    var tempFile = mock(File.class);
    when(graduateXlsxGenerator.generate(graduatesByTrack)).thenReturn(tempFile);
    when(bucketComponent.upload(any(), anyString())).thenReturn(null);
    when(bucketComponent.presign(anyString(), any()))
        .thenReturn(new URL("https://s3.example.com/file.xlsx"));

    var result = graduateExportService.exportToXlsx("2025-2026");

    assertNotNull(result);
    assertTrue(result.contains("https://s3.example.com/file.xlsx"));
  }
}

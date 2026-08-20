package com.example.demo.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.demo.endpoint.event.model.TranscriptEmailRequested;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.mail.Mailer;
import com.example.demo.model.User;
import com.example.demo.model.transcript.FullTranscript;
import com.example.demo.model.transcript.TranscriptStatus;
import com.example.demo.service.event.TranscriptEmailRequestedService;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptEmailRequestedServiceTest {

  @Mock private TranscriptService transcriptService;
  @Mock private TranscriptPdfGenerator transcriptPdfGenerator;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;
  @InjectMocks private TranscriptEmailRequestedService transcriptEmailRequestedService;

  @Test
  void accept() throws Exception {
    var studentId = UUID.randomUUID();
    var event = TranscriptEmailRequested.builder().studentId(studentId).build();

    var student =
        User.builder()
            .id(studentId)
            .lastName("Rakoto")
            .firstName("Jean")
            .email("jean@test.com")
            .build();
    var transcript =
        FullTranscript.builder()
            .student(student)
            .years(List.of())
            .overallAverage(null)
            .totalCredits(0)
            .status(TranscriptStatus.PROVISIONAL)
            .build();
    when(transcriptService.fullTranscript(studentId)).thenReturn(transcript);

    var pdfFile = mock(File.class);
    when(transcriptPdfGenerator.generate(transcript)).thenReturn(pdfFile);
    when(bucketComponent.upload(any(), anyString())).thenReturn(null);
    when(bucketComponent.presign(anyString(), any()))
        .thenReturn(new URL("https://s3.example.com/transcript.pdf"));

    transcriptEmailRequestedService.accept(event);

    verify(transcriptService).fullTranscript(studentId);
    verify(transcriptPdfGenerator).generate(transcript);
    verify(bucketComponent).upload(eq(pdfFile), anyString());
    verify(bucketComponent).presign(anyString(), any());
    verify(mailer).accept(any());
  }
}

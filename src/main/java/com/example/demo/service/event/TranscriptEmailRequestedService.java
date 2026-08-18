package com.example.demo.service.event;

import com.example.demo.endpoint.event.model.TranscriptEmailRequested;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.model.User;
import com.example.demo.service.TranscriptPdfGenerator;
import com.example.demo.service.TranscriptService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TranscriptEmailRequestedService implements Consumer<TranscriptEmailRequested> {

  private static final String BUCKET_PREFIX = "transcripts/";
  private static final Duration DOWNLOAD_LINK_DURATION = Duration.ofMinutes(15);

  private final TranscriptService transcriptService;
  private final TranscriptPdfGenerator transcriptPdfGenerator;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @Override
  public void accept(TranscriptEmailRequested event) {
    var transcript = transcriptService.fullTranscript(event.getStudentId());
    var pdf = transcriptPdfGenerator.generate(transcript);

    var bucketKey = BUCKET_PREFIX + event.getStudentId() + "-" + LocalDate.now() + ".pdf";
    bucketComponent.upload(pdf, bucketKey);

    var downloadUrl = bucketComponent.presign(bucketKey, DOWNLOAD_LINK_DURATION).toString();
    mailer.accept(toEmail(transcript.student(), downloadUrl));
  }

  private Email toEmail(User student, String downloadUrl) {
    try {
      return new Email(
          new InternetAddress(student.email()),
          List.of(),
          List.of(),
          "Relevé de note " + student.reference(),
          "<p>Bonjour "
              + student.firstName()
              + " "
              + student.lastName()
              + ",</p>"
              + "<p>Vous pouvez télécharger votre relevé de notes via le lien suivant "
              + "(valable 15 minutes) :</p>"
              + "<p><a href=\""
              + downloadUrl
              + "\">Télécharger mon relevé de notes (PDF)</a></p>",
          List.of());
    } catch (AddressException e) {
      throw new RuntimeException("Invalid recipient email address: " + student.email(), e);
    }
  }
}

package com.example.demo.service;

import com.example.demo.model.transcript.FullTranscript;
import com.example.demo.model.transcript.TranscriptCourseLine;
import com.example.demo.model.transcript.YearTranscript;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

@Component
public class TranscriptPdfGenerator {

  private static final float MARGIN = 50f;
  private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
  private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
  private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;

  private static final DateTimeFormatter SIGNATURE_DATE_FORMAT =
      DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

  public File generate(FullTranscript transcript) {
    try (var document = new PDDocument()) {
      for (var year : transcript.years()) {
        renderYearPage(document, transcript, year);
      }
      renderSummaryPage(document, transcript);

      var file = File.createTempFile("transcript-", ".pdf");
      document.save(file);
      return file;
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate transcript PDF", e);
    }
  }

  private void renderYearPage(PDDocument document, FullTranscript transcript, YearTranscript year)
      throws IOException {
    var page = new PDPage(PDRectangle.A4);
    document.addPage(page);

    try (var cs = new PDPageContentStream(document, page)) {
      float y = PAGE_HEIGHT - MARGIN;

      y = SchoolHeader.render(cs, y);
      y = writeCenteredTitle(cs, y, "Relevé de notes - Année " + year.academicYear().label());
      y -= 20;

      y = writeStudentInfo(cs, y, transcript);
      y -= 15;

      y =
          new TableRenderer(cs, MARGIN, y, CONTENT_WIDTH)
              .header("Code UE", "Intitulé UE", "Crédits acquérables", "Note sur 20")
              .columnWidths(0.13f, 0.45f, 0.24f, 0.18f)
              .rows(courseRows(year.courses()))
              .draw();
      y -= 20;

      y =
          writeResult(
              cs,
              y,
              year.validatedCredits(),
              year.totalCredits(),
              year.generalAverage(),
              "Moyenne annuelle pondérée par les crédits");

      writeSignatureBlock(cs);
    }
  }

  private void renderSummaryPage(PDDocument document, FullTranscript transcript)
      throws IOException {
    var page = new PDPage(PDRectangle.A4);
    document.addPage(page);

    try (var cs = new PDPageContentStream(document, page)) {
      float y = PAGE_HEIGHT - MARGIN;

      y = SchoolHeader.render(cs, y);
      y = writeCenteredTitle(cs, y, "Synthèse du cursus");
      y -= 20;

      y = writeStudentInfo(cs, y, transcript);
      y -= 15;

      writeText(cs, MARGIN, y, PDType1Font.HELVETICA_BOLD, 11, "Statut : " + transcript.status());
      y -= 20;

      y =
          new TableRenderer(cs, MARGIN, y, CONTENT_WIDTH)
              .header("Année", "Crédits validés", "Moyenne annuelle", "Statut")
              .columnWidths(0.30f, 0.25f, 0.25f, 0.20f)
              .rows(yearRows(transcript.years()))
              .draw();
      y -= 20;

      y =
          writeResult(
              cs,
              y,
              null,
              transcript.totalCredits(),
              transcript.overallAverage(),
              "Moyenne générale pondérée par les crédits");

      writeSignatureBlock(cs);
    }
  }

  private List<String[]> yearRows(List<YearTranscript> years) {
    return years.stream()
        .map(
            y ->
                new String[] {
                  y.academicYear().label(),
                  y.validatedCredits() + " / " + y.totalCredits(),
                  formatAverage(y.generalAverage()),
                  y.status().toString()
                })
        .toList();
  }

  private List<String[]> courseRows(List<TranscriptCourseLine> courses) {
    return courses.stream()
        .map(
            c ->
                new String[] {
                  c.courseRef(),
                  c.courseTitle(),
                  String.valueOf(c.creditCount()),
                  formatAverage(c.average())
                })
        .toList();
  }

  private float writeStudentInfo(PDPageContentStream cs, float y, FullTranscript transcript)
      throws IOException {
    var student = transcript.student();
    writeText(cs, MARGIN, y, PDType1Font.HELVETICA_BOLD, 10, "Nom : ");
    writeText(cs, MARGIN + 90, y, PDType1Font.HELVETICA, 10, student.lastName());
    y -= 15;
    writeText(cs, MARGIN, y, PDType1Font.HELVETICA_BOLD, 10, "Prénom(s) : ");
    writeText(cs, MARGIN + 90, y, PDType1Font.HELVETICA, 10, student.firstName());
    y -= 15;
    return y;
  }

  private float writeResult(
      PDPageContentStream cs,
      float y,
      Integer validatedCredits,
      int totalCredits,
      BigDecimal average,
      String averageLabel)
      throws IOException {
    writeText(cs, MARGIN, y, PDType1Font.HELVETICA_BOLD, 11, "Résultat");
    y -= 18;
    if (validatedCredits != null) {
      writeText(
          cs,
          MARGIN + 15,
          y,
          PDType1Font.HELVETICA,
          10,
          "•  Crédits acquis : " + validatedCredits + " / " + totalCredits);
      y -= 15;
    } else {
      writeText(
          cs, MARGIN + 15, y, PDType1Font.HELVETICA, 10, "•  Crédits totaux : " + totalCredits);
      y -= 15;
    }
    writeText(
        cs,
        MARGIN + 15,
        y,
        PDType1Font.HELVETICA,
        10,
        "•  " + averageLabel + " : " + formatAverage(average) + " / 20");
    y -= 15;
    return y;
  }

  private void writeSignatureBlock(PDPageContentStream cs) throws IOException {
    float y = MARGIN + 60;
    float x = PAGE_WIDTH - MARGIN - 220;
    var today = SIGNATURE_DATE_FORMAT.format(LocalDate.now());
    writeText(cs, x, y, PDType1Font.HELVETICA, 10, "Fait à Antananarivo, le " + today);
    y -= 15;
    writeText(cs, x, y, PDType1Font.HELVETICA, 10, "Le directeur pédagogique");
  }

  private float writeCenteredTitle(PDPageContentStream cs, float y, String text)
      throws IOException {
    var font = PDType1Font.HELVETICA_BOLD;
    int size = 14;
    float width = font.getStringWidth(text) / 1000 * size;
    float x = (PAGE_WIDTH - width) / 2;
    writeText(cs, x, y, font, size, text);
    return y - 24;
  }

  private void writeText(
      PDPageContentStream cs, float x, float y, PDType1Font font, int size, String text)
      throws IOException {
    cs.beginText();
    cs.setFont(font, size);
    cs.newLineAtOffset(x, y);
    cs.showText(text);
    cs.endText();
  }

  private String formatAverage(BigDecimal average) {
    return average == null ? "N/A" : average.toString();
  }

  private static final class SchoolHeader {
    private static final String SCHOOL_NAME = "Haute École d'Informatique";
    private static final String ADDRESS = "Lot 2J 161 R Ivandry, 101 Antananarivo, Madagascar";
    private static final String PHONE = "+261 34 94 041 16";
    private static final String EMAIL = "contact@mail.hei.school";

    static float render(PDPageContentStream cs, float y) throws IOException {
      y = writeCenteredLine(cs, y, SCHOOL_NAME, PDType1Font.HELVETICA_BOLD, 12);
      y -= 4;
      y = writeCenteredLine(cs, y, ADDRESS, PDType1Font.HELVETICA, 9);
      y -= 4;
      y = writeCenteredLine(cs, y, PHONE, PDType1Font.HELVETICA, 9);
      y -= 4;
      y = writeCenteredLine(cs, y, EMAIL, PDType1Font.HELVETICA, 9);
      return y - 20;
    }

    private static float writeCenteredLine(
        PDPageContentStream cs, float y, String text, PDType1Font font, int size)
        throws IOException {
      float width = font.getStringWidth(text) / 1000 * size;
      float x = (PAGE_WIDTH - width) / 2;
      cs.beginText();
      cs.setFont(font, size);
      cs.newLineAtOffset(x, y);
      cs.showText(text);
      cs.endText();
      return y - (size + 2);
    }
  }

  private static final class TableRenderer {
    private static final float ROW_PADDING = 6f;
    private static final float LINE_HEIGHT = 12f;

    private final PDPageContentStream cs;
    private final float x;
    private float y;
    private final float tableWidth;
    private String[] headers;
    private float[] columnRatios;
    private List<String[]> rows;

    TableRenderer(PDPageContentStream cs, float x, float y, float tableWidth) {
      this.cs = cs;
      this.x = x;
      this.y = y;
      this.tableWidth = tableWidth;
    }

    TableRenderer header(String... headers) {
      this.headers = headers;
      return this;
    }

    TableRenderer columnWidths(float... ratios) {
      this.columnRatios = ratios;
      return this;
    }

    TableRenderer rows(List<String[]> rows) {
      this.rows = rows;
      return this;
    }

    float draw() throws IOException {
      float[] colWidths = new float[columnRatios.length];
      for (int i = 0; i < columnRatios.length; i++) {
        colWidths[i] = tableWidth * columnRatios[i];
      }

      y = drawRow(headers, colWidths, PDType1Font.HELVETICA_BOLD, true);
      for (String[] row : rows) {
        y = drawRow(row, colWidths, PDType1Font.HELVETICA, false);
      }
      return y;
    }

    private float drawRow(String[] cells, float[] colWidths, PDType1Font font, boolean isHeader)
        throws IOException {
      float rowHeight = LINE_HEIGHT + 2 * ROW_PADDING;
      float rowTop = y;
      float rowBottom = y - rowHeight;

      if (isHeader) {
        cs.setNonStrokingColor(230, 176, 46); // teinte orangée proche du modèle HEI
        cs.addRect(x, rowBottom, tableWidth, rowHeight);
        cs.fill();
        cs.setNonStrokingColor(0, 0, 0);
      }

      float cellX = x;
      for (int i = 0; i < cells.length; i++) {
        cs.setLineWidth(0.5f);
        cs.addRect(cellX, rowBottom, colWidths[i], rowHeight);
        cs.stroke();

        int fontSize = isHeader ? 8 : 9;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(cellX + 5, rowTop - ROW_PADDING - 9);
        cs.showText(cells[i]);
        cs.endText();

        cellX += colWidths[i];
      }
      return rowBottom;
    }
  }
}

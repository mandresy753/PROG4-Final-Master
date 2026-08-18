package com.example.demo.service;

import com.example.demo.enums.Track;
import com.example.demo.model.Graduate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class GraduateXlsxGenerator {

  private static final List<Track> GRADUATION_TRACKS = List.of(Track.EL, Track.TN);

  private static final List<String> HEADERS =
      List.of("Rang", "Matricule", "Nom", "Prénom", "Moyenne générale");

  private static final int STREAMING_WINDOW_SIZE = 200;

  public File generate(Map<Track, List<Graduate>> graduatesByTrack) {
    var workbook = new SXSSFWorkbook(STREAMING_WINDOW_SIZE);
    try {
      var styles = new Styles(workbook);

      for (Track track : GRADUATION_TRACKS) {
        var sheet = workbook.createSheet(track.name());
        writeHeader(sheet, styles);
        writeRows(sheet, graduatesByTrack.getOrDefault(track, List.of()), styles);
        applyColumnWidths(sheet);
      }

      var file = File.createTempFile("graduates-", ".xlsx");
      try (var out = new FileOutputStream(file)) {
        workbook.write(out);
      }
      return file;
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate graduates XLSX", e);
    } finally {
      workbook.dispose();
    }
  }

  private void writeHeader(Sheet sheet, Styles styles) {
    var header = sheet.createRow(0);
    for (int i = 0; i < HEADERS.size(); i++) {
      var cell = header.createCell(i);
      cell.setCellValue(HEADERS.get(i));
      cell.setCellStyle(styles.header());
    }
    sheet.createFreezePane(0, 1);
  }

  private void writeRows(Sheet sheet, List<Graduate> graduates, Styles styles) {
    for (int i = 0; i < graduates.size(); i++) {
      writeRow(sheet.createRow(i + 1), graduates.get(i), styles);
    }
  }

  private void writeRow(Row row, Graduate graduate, Styles styles) {
    var student = graduate.student();

    row.createCell(0).setCellValue(graduate.rank());
    row.createCell(1).setCellValue(student.reference());
    row.createCell(2).setCellValue(student.lastName());
    row.createCell(3).setCellValue(student.firstName());

    var averageCell = row.createCell(4);
    averageCell.setCellValue(
        graduate.overallAverage() == null ? 0 : graduate.overallAverage().doubleValue());
    averageCell.setCellStyle(styles.average());

    for (int col : new int[] {0, 1, 2, 3}) {
      row.getCell(col).setCellStyle(styles.data());
    }
  }

  private void applyColumnWidths(Sheet sheet) {
    sheet.setColumnWidth(0, 6000);
    sheet.setColumnWidth(1, 6000);
    sheet.setColumnWidth(2, 6000);
    sheet.setColumnWidth(3, 6000);
    sheet.setColumnWidth(4, 6000);
  }

  private static final class Styles {
    private final CellStyle header;
    private final CellStyle data;
    private final CellStyle average;

    Styles(SXSSFWorkbook workbook) {
      var boldFont = workbook.createFont();
      boldFont.setBold(true);

      header = workbook.createCellStyle();
      header.setFont(boldFont);
      header.setAlignment(HorizontalAlignment.CENTER);

      data = workbook.createCellStyle();
      data.setAlignment(HorizontalAlignment.CENTER);

      average = workbook.createCellStyle();
      average.setAlignment(HorizontalAlignment.CENTER);
      average.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
    }

    CellStyle header() {
      return header;
    }

    CellStyle data() {
      return data;
    }

    CellStyle average() {
      return average;
    }
  }
}

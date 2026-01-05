package com.mzc.lp.domain.user.util;

import com.mzc.lp.domain.user.dto.request.FileUserRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class UserExcelParser {

    private static final int MAX_ROWS = 500;

    public List<FileUserRow> parseExcel(MultipartFile file) throws IOException {
        List<FileUserRow> users = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 0;

            for (Row row : sheet) {
                // 첫 번째 행은 헤더로 스킵
                if (row.getRowNum() == 0) {
                    continue;
                }

                if (rowCount >= MAX_ROWS) {
                    log.warn("Maximum row limit ({}) exceeded, stopping parsing", MAX_ROWS);
                    break;
                }

                FileUserRow userRow = parseRow(row);
                if (userRow != null && userRow.email() != null && !userRow.email().isBlank()) {
                    users.add(userRow);
                    rowCount++;
                }
            }
        }

        return users;
    }

    public List<FileUserRow> parseCsv(MultipartFile file) throws IOException {
        List<FileUserRow> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNum = 0;
            int rowCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;

                // 첫 번째 줄은 헤더로 스킵
                if (lineNum == 1) {
                    continue;
                }

                if (rowCount >= MAX_ROWS) {
                    log.warn("Maximum row limit ({}) exceeded, stopping parsing", MAX_ROWS);
                    break;
                }

                FileUserRow userRow = parseCsvLine(line);
                if (userRow != null && userRow.email() != null && !userRow.email().isBlank()) {
                    users.add(userRow);
                    rowCount++;
                }
            }
        }

        return users;
    }

    private FileUserRow parseRow(Row row) {
        try {
            String email = getCellValueAsString(row.getCell(0));
            String name = getCellValueAsString(row.getCell(1));
            String password = getCellValueAsString(row.getCell(2));
            String phone = getCellValueAsString(row.getCell(3));

            if (email == null || email.isBlank()) {
                return null;
            }

            return new FileUserRow(
                    email.trim().toLowerCase(),
                    name != null ? name.trim() : null,
                    password != null ? password.trim() : null,
                    phone != null ? phone.trim() : null
            );
        } catch (Exception e) {
            log.warn("Failed to parse row {}: {}", row.getRowNum(), e.getMessage());
            return null;
        }
    }

    private FileUserRow parseCsvLine(String line) {
        try {
            String[] parts = line.split(",", -1);

            String email = parts.length > 0 ? parts[0].trim() : null;
            String name = parts.length > 1 ? parts[1].trim() : null;
            String password = parts.length > 2 ? parts[2].trim() : null;
            String phone = parts.length > 3 ? parts[3].trim() : null;

            if (email == null || email.isBlank()) {
                return null;
            }

            return new FileUserRow(
                    email.toLowerCase(),
                    name,
                    password,
                    phone
            );
        } catch (Exception e) {
            log.warn("Failed to parse CSV line: {}", e.getMessage());
            return null;
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
}

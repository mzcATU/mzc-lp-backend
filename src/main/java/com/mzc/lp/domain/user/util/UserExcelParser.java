package com.mzc.lp.domain.user.util;

import com.mzc.lp.domain.user.dto.request.FileParseResult;
import com.mzc.lp.domain.user.dto.request.FileParseResult.ParseError;
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
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class UserExcelParser {

    private static final int MAX_ROWS = 500;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    // 허용되는 헤더명 (대소문자 무시, 한글/영문 모두 지원)
    private static final Set<String> EMAIL_HEADERS = Set.of("email", "이메일", "e-mail", "메일");
    private static final Set<String> NAME_HEADERS = Set.of("name", "이름", "성명", "사용자명");
    private static final Set<String> PASSWORD_HEADERS = Set.of("password", "비밀번호", "패스워드", "pw");
    private static final Set<String> PHONE_HEADERS = Set.of("phone", "전화번호", "휴대폰", "연락처", "tel");
    private static final Set<String> DEPARTMENT_HEADERS = Set.of("department", "부서", "소속", "팀");
    private static final Set<String> POSITION_HEADERS = Set.of("position", "직급", "직위", "직책");

    /**
     * Excel 파일 파싱 (헤더 기반 매핑, 검증 포함)
     */
    public FileParseResult parseExcelWithValidation(MultipartFile file) throws IOException {
        List<FileUserRow> users = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // 헤더 행 파싱
            if (!rowIterator.hasNext()) {
                return FileParseResult.headerError("파일이 비어있습니다");
            }

            Row headerRow = rowIterator.next();
            Map<String, Integer> columnMap = parseHeaderRow(headerRow);

            // 필수 컬럼 검증
            if (!columnMap.containsKey("email")) {
                return FileParseResult.headerError("필수 컬럼 'email'이 없습니다. 헤더에 'email' 또는 '이메일' 컬럼을 추가해주세요.");
            }

            int rowCount = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                int rowNum = row.getRowNum() + 1; // 1-based for user display

                if (rowCount >= MAX_ROWS) {
                    log.warn("Maximum row limit ({}) exceeded, stopping parsing", MAX_ROWS);
                    errors.add(new ParseError(rowNum, "system", "",
                            String.format("최대 %d개 행까지만 처리됩니다", MAX_ROWS)));
                    break;
                }

                FileUserRow userRow = parseDataRow(row, columnMap, rowNum, errors);
                if (userRow != null) {
                    users.add(userRow);
                    rowCount++;
                }
            }
        }

        return FileParseResult.withErrors(users, errors);
    }

    /**
     * CSV 파일 파싱 (헤더 기반 매핑, 검증 포함)
     */
    public FileParseResult parseCsvWithValidation(MultipartFile file) throws IOException {
        List<FileUserRow> users = new ArrayList<>();
        List<ParseError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // 헤더 행 읽기
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                return FileParseResult.headerError("파일이 비어있습니다");
            }

            Map<String, Integer> columnMap = parseCsvHeader(headerLine);

            // 필수 컬럼 검증
            if (!columnMap.containsKey("email")) {
                return FileParseResult.headerError("필수 컬럼 'email'이 없습니다. 헤더에 'email' 또는 '이메일' 컬럼을 추가해주세요.");
            }

            String line;
            int lineNum = 1; // 헤더가 1, 데이터는 2부터
            int rowCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;

                if (rowCount >= MAX_ROWS) {
                    log.warn("Maximum row limit ({}) exceeded, stopping parsing", MAX_ROWS);
                    errors.add(new ParseError(lineNum, "system", "",
                            String.format("최대 %d개 행까지만 처리됩니다", MAX_ROWS)));
                    break;
                }

                FileUserRow userRow = parseCsvDataLine(line, columnMap, lineNum, errors);
                if (userRow != null) {
                    users.add(userRow);
                    rowCount++;
                }
            }
        }

        return FileParseResult.withErrors(users, errors);
    }

    /**
     * 기존 Excel 파싱 메서드 (하위 호환 - 고정 컬럼 순서)
     */
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

                FileUserRow userRow = parseRowLegacy(row);
                if (userRow != null && userRow.email() != null && !userRow.email().isBlank()) {
                    users.add(userRow);
                    rowCount++;
                }
            }
        }

        return users;
    }

    /**
     * 기존 CSV 파싱 메서드 (하위 호환 - 고정 컬럼 순서)
     */
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

                FileUserRow userRow = parseCsvLineLegacy(line);
                if (userRow != null && userRow.email() != null && !userRow.email().isBlank()) {
                    users.add(userRow);
                    rowCount++;
                }
            }
        }

        return users;
    }

    // ========== Private Helper Methods ==========

    private Map<String, Integer> parseHeaderRow(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();

        for (Cell cell : headerRow) {
            String headerValue = getCellValueAsString(cell);
            if (headerValue == null || headerValue.isBlank()) {
                continue;
            }

            String normalizedHeader = headerValue.trim().toLowerCase();
            int colIndex = cell.getColumnIndex();

            if (matchesAny(normalizedHeader, EMAIL_HEADERS)) {
                columnMap.put("email", colIndex);
            } else if (matchesAny(normalizedHeader, NAME_HEADERS)) {
                columnMap.put("name", colIndex);
            } else if (matchesAny(normalizedHeader, PASSWORD_HEADERS)) {
                columnMap.put("password", colIndex);
            } else if (matchesAny(normalizedHeader, PHONE_HEADERS)) {
                columnMap.put("phone", colIndex);
            } else if (matchesAny(normalizedHeader, DEPARTMENT_HEADERS)) {
                columnMap.put("department", colIndex);
            } else if (matchesAny(normalizedHeader, POSITION_HEADERS)) {
                columnMap.put("position", colIndex);
            }
        }

        log.debug("Parsed header columns: {}", columnMap);
        return columnMap;
    }

    private Map<String, Integer> parseCsvHeader(String headerLine) {
        Map<String, Integer> columnMap = new HashMap<>();
        String[] headers = headerLine.split(",", -1);

        for (int i = 0; i < headers.length; i++) {
            String normalizedHeader = headers[i].trim().toLowerCase();

            if (matchesAny(normalizedHeader, EMAIL_HEADERS)) {
                columnMap.put("email", i);
            } else if (matchesAny(normalizedHeader, NAME_HEADERS)) {
                columnMap.put("name", i);
            } else if (matchesAny(normalizedHeader, PASSWORD_HEADERS)) {
                columnMap.put("password", i);
            } else if (matchesAny(normalizedHeader, PHONE_HEADERS)) {
                columnMap.put("phone", i);
            } else if (matchesAny(normalizedHeader, DEPARTMENT_HEADERS)) {
                columnMap.put("department", i);
            } else if (matchesAny(normalizedHeader, POSITION_HEADERS)) {
                columnMap.put("position", i);
            }
        }

        log.debug("Parsed CSV header columns: {}", columnMap);
        return columnMap;
    }

    private boolean matchesAny(String value, Set<String> candidates) {
        return candidates.contains(value);
    }

    private FileUserRow parseDataRow(Row row, Map<String, Integer> columnMap, int rowNum, List<ParseError> errors) {
        try {
            String email = getColumnValue(row, columnMap, "email");
            String name = getColumnValue(row, columnMap, "name");
            String password = getColumnValue(row, columnMap, "password");
            String phone = getColumnValue(row, columnMap, "phone");
            String department = getColumnValue(row, columnMap, "department");
            String position = getColumnValue(row, columnMap, "position");

            // 빈 행 스킵
            if ((email == null || email.isBlank()) && (name == null || name.isBlank())) {
                return null;
            }

            // 이메일 필수 검증
            if (email == null || email.isBlank()) {
                errors.add(new ParseError(rowNum, "email", "", "이메일이 비어있습니다"));
                return null;
            }

            // 이메일 형식 검증
            email = email.trim().toLowerCase();
            if (!isValidEmail(email)) {
                errors.add(new ParseError(rowNum, "email", email, "이메일 형식이 올바르지 않습니다"));
                return null;
            }

            return new FileUserRow(
                    email,
                    name != null ? name.trim() : null,
                    password != null ? password.trim() : null,
                    phone != null ? phone.trim() : null,
                    department != null ? department.trim() : null,
                    position != null ? position.trim() : null
            );
        } catch (Exception e) {
            log.warn("Failed to parse row {}: {}", rowNum, e.getMessage());
            errors.add(new ParseError(rowNum, "row", "", "행 파싱 실패: " + e.getMessage()));
            return null;
        }
    }

    private FileUserRow parseCsvDataLine(String line, Map<String, Integer> columnMap, int rowNum, List<ParseError> errors) {
        try {
            String[] parts = line.split(",", -1);

            String email = getColumnValue(parts, columnMap, "email");
            String name = getColumnValue(parts, columnMap, "name");
            String password = getColumnValue(parts, columnMap, "password");
            String phone = getColumnValue(parts, columnMap, "phone");
            String department = getColumnValue(parts, columnMap, "department");
            String position = getColumnValue(parts, columnMap, "position");

            // 빈 행 스킵
            if ((email == null || email.isBlank()) && (name == null || name.isBlank())) {
                return null;
            }

            // 이메일 필수 검증
            if (email == null || email.isBlank()) {
                errors.add(new ParseError(rowNum, "email", "", "이메일이 비어있습니다"));
                return null;
            }

            // 이메일 형식 검증
            email = email.trim().toLowerCase();
            if (!isValidEmail(email)) {
                errors.add(new ParseError(rowNum, "email", email, "이메일 형식이 올바르지 않습니다"));
                return null;
            }

            return new FileUserRow(
                    email,
                    name != null ? name.trim() : null,
                    password != null ? password.trim() : null,
                    phone != null ? phone.trim() : null,
                    department != null ? department.trim() : null,
                    position != null ? position.trim() : null
            );
        } catch (Exception e) {
            log.warn("Failed to parse CSV line {}: {}", rowNum, e.getMessage());
            errors.add(new ParseError(rowNum, "row", "", "행 파싱 실패: " + e.getMessage()));
            return null;
        }
    }

    private String getColumnValue(Row row, Map<String, Integer> columnMap, String columnName) {
        Integer colIndex = columnMap.get(columnName);
        if (colIndex == null) {
            return null;
        }
        return getCellValueAsString(row.getCell(colIndex));
    }

    private String getColumnValue(String[] parts, Map<String, Integer> columnMap, String columnName) {
        Integer colIndex = columnMap.get(columnName);
        if (colIndex == null || colIndex >= parts.length) {
            return null;
        }
        String value = parts[colIndex].trim();
        return value.isEmpty() ? null : value;
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // Legacy methods (고정 컬럼 순서)
    private FileUserRow parseRowLegacy(Row row) {
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

    private FileUserRow parseCsvLineLegacy(String line) {
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

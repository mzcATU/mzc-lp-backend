package com.mzc.lp.domain.analytics.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mzc.lp.domain.analytics.constant.ExportFormat;
import com.mzc.lp.domain.analytics.constant.ReportPeriod;
import com.mzc.lp.domain.analytics.constant.ReportType;
import com.mzc.lp.domain.analytics.dto.response.ExportJobResponse;
import com.mzc.lp.domain.analytics.dto.response.ExportStatsResponse;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 리포트 내보내기 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReportExportServiceImpl implements ReportExportService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    @Override
    public void exportReport(Long tenantId, ReportType reportType, ExportFormat format,
                             ReportPeriod period, OutputStream outputStream) {
        log.info("Exporting report: type={}, format={}, period={}, tenantId={}",
                reportType, format, period, tenantId);

        try {
            switch (format) {
                case CSV -> exportToCSV(tenantId, reportType, period, outputStream);
                case XLSX -> exportToExcel(tenantId, reportType, period, outputStream);
                case PDF -> exportToPDF(tenantId, reportType, period, outputStream);
            }
        } catch (IOException e) {
            log.error("Failed to write report: {}", e.getMessage());
            throw new RuntimeException("리포트 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * CSV 형식 내보내기
     */
    private void exportToCSV(Long tenantId, ReportType reportType, ReportPeriod period, OutputStream outputStream) throws IOException {
        // BOM for UTF-8 Excel compatibility
        outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

        switch (reportType) {
            case USERS -> exportUsersReportCSV(tenantId, period, writer);
            case COURSES -> exportCoursesReportCSV(tenantId, period, writer);
            case LEARNING -> exportLearningReportCSV(tenantId, period, writer);
            case COMPLETION -> exportCompletionReportCSV(tenantId, period, writer);
            case ENGAGEMENT -> exportEngagementReportCSV(tenantId, period, writer);
        }

        writer.flush();
    }

    /**
     * Excel (XLSX) 형식 내보내기
     */
    private void exportToExcel(Long tenantId, ReportType reportType, ReportPeriod period, OutputStream outputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(reportType.getName());

            // 헤더 스타일 생성
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            switch (reportType) {
                case USERS -> exportUsersReportExcel(tenantId, period, sheet, headerStyle);
                case COURSES -> exportCoursesReportExcel(tenantId, period, sheet, headerStyle);
                case LEARNING -> exportLearningReportExcel(tenantId, period, sheet, headerStyle);
                case COMPLETION -> exportCompletionReportExcel(tenantId, period, sheet, headerStyle);
                case ENGAGEMENT -> exportEngagementReportExcel(tenantId, period, sheet, headerStyle);
            }

            // 컬럼 너비 자동 조정
            if (sheet.getRow(0) != null) {
                for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(outputStream);
        }
    }

    /**
     * PDF 형식 내보내기
     */
    private void exportToPDF(Long tenantId, ReportType reportType, ReportPeriod period, OutputStream outputStream) throws IOException {
        Document document = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // 한글 폰트 설정 (시스템 폰트 사용)
            Font titleFont = createKoreanFont(16, Font.BOLD);
            Font headerFont = createKoreanFont(10, Font.BOLD);
            Font contentFont = createKoreanFont(9, Font.NORMAL);

            // 제목 추가
            Paragraph title = new Paragraph(reportType.getName() + " 리포트", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // 생성일시 추가
            Paragraph dateInfo = new Paragraph("생성일시: " + DATE_FORMATTER.format(Instant.now()), contentFont);
            dateInfo.setAlignment(Element.ALIGN_RIGHT);
            dateInfo.setSpacingAfter(10);
            document.add(dateInfo);

            switch (reportType) {
                case USERS -> exportUsersReportPDF(tenantId, period, document, headerFont, contentFont);
                case COURSES -> exportCoursesReportPDF(tenantId, period, document, headerFont, contentFont);
                case LEARNING -> exportLearningReportPDF(tenantId, period, document, headerFont, contentFont);
                case COMPLETION -> exportCompletionReportPDF(tenantId, period, document, headerFont, contentFont);
                case ENGAGEMENT -> exportEngagementReportPDF(tenantId, period, document, headerFont, contentFont);
            }

        } catch (DocumentException e) {
            throw new IOException("PDF 생성 실패", e);
        } finally {
            document.close();
        }
    }

    /**
     * 한글 지원 폰트 생성
     */
    private Font createKoreanFont(int size, int style) {
        try {
            // Windows 시스템 폰트 경로
            String fontPath = "C:/Windows/Fonts/malgun.ttf";
            BaseFont baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(baseFont, size, style);
        } catch (Exception e) {
            log.warn("한글 폰트를 찾을 수 없어 기본 폰트를 사용합니다: {}", e.getMessage());
            return new Font(Font.HELVETICA, size, style);
        }
    }

    @Override
    public List<ExportJobResponse> getExportHistory(Long tenantId, int limit) {
        // 내보내기 이력은 실시간 생성이므로 빈 목록 반환
        // TODO: 이력 저장 기능 추가 시 구현
        return List.of();
    }

    @Override
    public ExportStatsResponse getExportStats(Long tenantId) {
        // 기본 통계 반환
        // TODO: 실제 이력 기반 통계 구현 시 업데이트
        return ExportStatsResponse.of(0, "0 MB", "학습 진도");
    }

    // ============================================
    // CSV Export Methods
    // ============================================

    private void exportUsersReportCSV(Long tenantId, ReportPeriod period, PrintWriter writer) {
        writer.println("ID,이름,이메일,역할,상태,가입일");
        List<User> users = getUsersForExport(tenantId, period);
        for (User user : users) {
            writer.println(String.join(",",
                    escapeCSV(user.getId().toString()),
                    escapeCSV(user.getName()),
                    escapeCSV(user.getEmail()),
                    escapeCSV(user.getRole() != null ? user.getRole().name() : ""),
                    escapeCSV(user.getStatus() != null ? user.getStatus().name() : ""),
                    escapeCSV(formatInstant(user.getCreatedAt()))
            ));
        }
    }

    private void exportCoursesReportCSV(Long tenantId, ReportPeriod period, PrintWriter writer) {
        writer.println("ID,강좌명,카테고리ID,생성자ID,생성일");
        List<Course> courses = getCoursesForExport(tenantId, period);
        for (Course course : courses) {
            writer.println(String.join(",",
                    escapeCSV(course.getId().toString()),
                    escapeCSV(course.getTitle()),
                    escapeCSV(course.getCategoryId() != null ? course.getCategoryId().toString() : ""),
                    escapeCSV(course.getCreatedBy() != null ? course.getCreatedBy().toString() : ""),
                    escapeCSV(formatInstant(course.getCreatedAt()))
            ));
        }
    }

    private void exportLearningReportCSV(Long tenantId, ReportPeriod period, PrintWriter writer) {
        writer.println("수강ID,사용자ID,차수ID,진도율(%),상태,수강신청일");
        List<Enrollment> enrollments = getEnrollmentsForExport(tenantId, period);
        for (Enrollment enrollment : enrollments) {
            writer.println(String.join(",",
                    escapeCSV(enrollment.getId().toString()),
                    escapeCSV(enrollment.getUserId().toString()),
                    escapeCSV(enrollment.getCourseTimeId().toString()),
                    escapeCSV(enrollment.getProgressPercent() != null ? enrollment.getProgressPercent().toString() : "0"),
                    escapeCSV(enrollment.getStatus() != null ? enrollment.getStatus().name() : ""),
                    escapeCSV(formatInstant(enrollment.getEnrolledAt()))
            ));
        }
    }

    private void exportCompletionReportCSV(Long tenantId, ReportPeriod period, PrintWriter writer) {
        writer.println("수강ID,사용자ID,차수ID,점수,수료일,수강신청일");
        List<Enrollment> enrollments = getCompletedEnrollmentsForExport(tenantId, period);
        for (Enrollment enrollment : enrollments) {
            writer.println(String.join(",",
                    escapeCSV(enrollment.getId().toString()),
                    escapeCSV(enrollment.getUserId().toString()),
                    escapeCSV(enrollment.getCourseTimeId().toString()),
                    escapeCSV(enrollment.getScore() != null ? enrollment.getScore().toString() : ""),
                    escapeCSV(formatInstant(enrollment.getCompletedAt())),
                    escapeCSV(formatInstant(enrollment.getEnrolledAt()))
            ));
        }
    }

    private void exportEngagementReportCSV(Long tenantId, ReportPeriod period, PrintWriter writer) {
        writer.println("사용자ID,이름,이메일,총수강,완료,진행중,평균진도율(%)");
        List<User> users = getUsersForExport(tenantId, period);
        for (User user : users) {
            long[] stats = getUserEngagementStats(user.getId(), tenantId);
            writer.println(String.join(",",
                    escapeCSV(user.getId().toString()),
                    escapeCSV(user.getName()),
                    escapeCSV(user.getEmail()),
                    escapeCSV(String.valueOf(stats[0])),
                    escapeCSV(String.valueOf(stats[1])),
                    escapeCSV(String.valueOf(stats[2])),
                    escapeCSV(String.format("%.1f", (double) stats[3] / 10))
            ));
        }
    }

    // ============================================
    // Excel Export Methods
    // ============================================

    private void exportUsersReportExcel(Long tenantId, ReportPeriod period, Sheet sheet, CellStyle headerStyle) {
        String[] headers = {"ID", "이름", "이메일", "역할", "상태", "가입일"};
        createHeaderRow(sheet, headers, headerStyle);

        List<User> users = getUsersForExport(tenantId, period);
        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getEmail());
            row.createCell(3).setCellValue(user.getRole() != null ? user.getRole().name() : "");
            row.createCell(4).setCellValue(user.getStatus() != null ? user.getStatus().name() : "");
            row.createCell(5).setCellValue(formatInstant(user.getCreatedAt()));
        }
    }

    private void exportCoursesReportExcel(Long tenantId, ReportPeriod period, Sheet sheet, CellStyle headerStyle) {
        String[] headers = {"ID", "강좌명", "카테고리ID", "생성자ID", "생성일"};
        createHeaderRow(sheet, headers, headerStyle);

        List<Course> courses = getCoursesForExport(tenantId, period);
        int rowNum = 1;
        for (Course course : courses) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(course.getId());
            row.createCell(1).setCellValue(course.getTitle());
            row.createCell(2).setCellValue(course.getCategoryId() != null ? course.getCategoryId() : 0);
            row.createCell(3).setCellValue(course.getCreatedBy() != null ? course.getCreatedBy() : 0);
            row.createCell(4).setCellValue(formatInstant(course.getCreatedAt()));
        }
    }

    private void exportLearningReportExcel(Long tenantId, ReportPeriod period, Sheet sheet, CellStyle headerStyle) {
        String[] headers = {"수강ID", "사용자ID", "차수ID", "진도율(%)", "상태", "수강신청일"};
        createHeaderRow(sheet, headers, headerStyle);

        List<Enrollment> enrollments = getEnrollmentsForExport(tenantId, period);
        int rowNum = 1;
        for (Enrollment enrollment : enrollments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(enrollment.getId());
            row.createCell(1).setCellValue(enrollment.getUserId());
            row.createCell(2).setCellValue(enrollment.getCourseTimeId());
            row.createCell(3).setCellValue(enrollment.getProgressPercent() != null ? enrollment.getProgressPercent() : 0);
            row.createCell(4).setCellValue(enrollment.getStatus() != null ? enrollment.getStatus().name() : "");
            row.createCell(5).setCellValue(formatInstant(enrollment.getEnrolledAt()));
        }
    }

    private void exportCompletionReportExcel(Long tenantId, ReportPeriod period, Sheet sheet, CellStyle headerStyle) {
        String[] headers = {"수강ID", "사용자ID", "차수ID", "점수", "수료일", "수강신청일"};
        createHeaderRow(sheet, headers, headerStyle);

        List<Enrollment> enrollments = getCompletedEnrollmentsForExport(tenantId, period);
        int rowNum = 1;
        for (Enrollment enrollment : enrollments) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(enrollment.getId());
            row.createCell(1).setCellValue(enrollment.getUserId());
            row.createCell(2).setCellValue(enrollment.getCourseTimeId());
            row.createCell(3).setCellValue(enrollment.getScore() != null ? enrollment.getScore() : 0);
            row.createCell(4).setCellValue(formatInstant(enrollment.getCompletedAt()));
            row.createCell(5).setCellValue(formatInstant(enrollment.getEnrolledAt()));
        }
    }

    private void exportEngagementReportExcel(Long tenantId, ReportPeriod period, Sheet sheet, CellStyle headerStyle) {
        String[] headers = {"사용자ID", "이름", "이메일", "총수강", "완료", "진행중", "평균진도율(%)"};
        createHeaderRow(sheet, headers, headerStyle);

        List<User> users = getUsersForExport(tenantId, period);
        int rowNum = 1;
        for (User user : users) {
            long[] stats = getUserEngagementStats(user.getId(), tenantId);
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getEmail());
            row.createCell(3).setCellValue(stats[0]);
            row.createCell(4).setCellValue(stats[1]);
            row.createCell(5).setCellValue(stats[2]);
            row.createCell(6).setCellValue((double) stats[3] / 10);
        }
    }

    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    // ============================================
    // PDF Export Methods
    // ============================================

    private void exportUsersReportPDF(Long tenantId, ReportPeriod period, Document document,
                                       Font headerFont, Font contentFont) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2, 3, 1.5f, 1.5f, 2});

        addPDFTableHeader(table, new String[]{"ID", "이름", "이메일", "역할", "상태", "가입일"}, headerFont);

        List<User> users = getUsersForExport(tenantId, period);
        for (User user : users) {
            addPDFTableCell(table, user.getId().toString(), contentFont);
            addPDFTableCell(table, user.getName(), contentFont);
            addPDFTableCell(table, user.getEmail(), contentFont);
            addPDFTableCell(table, user.getRole() != null ? user.getRole().name() : "", contentFont);
            addPDFTableCell(table, user.getStatus() != null ? user.getStatus().name() : "", contentFont);
            addPDFTableCell(table, formatInstant(user.getCreatedAt()), contentFont);
        }

        document.add(table);
    }

    private void exportCoursesReportPDF(Long tenantId, ReportPeriod period, Document document,
                                         Font headerFont, Font contentFont) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 4, 1.5f, 1.5f, 2});

        addPDFTableHeader(table, new String[]{"ID", "강좌명", "카테고리ID", "생성자ID", "생성일"}, headerFont);

        List<Course> courses = getCoursesForExport(tenantId, period);
        for (Course course : courses) {
            addPDFTableCell(table, course.getId().toString(), contentFont);
            addPDFTableCell(table, course.getTitle(), contentFont);
            addPDFTableCell(table, course.getCategoryId() != null ? course.getCategoryId().toString() : "", contentFont);
            addPDFTableCell(table, course.getCreatedBy() != null ? course.getCreatedBy().toString() : "", contentFont);
            addPDFTableCell(table, formatInstant(course.getCreatedAt()), contentFont);
        }

        document.add(table);
    }

    private void exportLearningReportPDF(Long tenantId, ReportPeriod period, Document document,
                                          Font headerFont, Font contentFont) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 2});

        addPDFTableHeader(table, new String[]{"수강ID", "사용자ID", "차수ID", "진도율(%)", "상태", "수강신청일"}, headerFont);

        List<Enrollment> enrollments = getEnrollmentsForExport(tenantId, period);
        for (Enrollment enrollment : enrollments) {
            addPDFTableCell(table, enrollment.getId().toString(), contentFont);
            addPDFTableCell(table, enrollment.getUserId().toString(), contentFont);
            addPDFTableCell(table, enrollment.getCourseTimeId().toString(), contentFont);
            addPDFTableCell(table, enrollment.getProgressPercent() != null ? enrollment.getProgressPercent().toString() : "0", contentFont);
            addPDFTableCell(table, enrollment.getStatus() != null ? enrollment.getStatus().name() : "", contentFont);
            addPDFTableCell(table, formatInstant(enrollment.getEnrolledAt()), contentFont);
        }

        document.add(table);
    }

    private void exportCompletionReportPDF(Long tenantId, ReportPeriod period, Document document,
                                            Font headerFont, Font contentFont) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1, 2, 2});

        addPDFTableHeader(table, new String[]{"수강ID", "사용자ID", "차수ID", "점수", "수료일", "수강신청일"}, headerFont);

        List<Enrollment> enrollments = getCompletedEnrollmentsForExport(tenantId, period);
        for (Enrollment enrollment : enrollments) {
            addPDFTableCell(table, enrollment.getId().toString(), contentFont);
            addPDFTableCell(table, enrollment.getUserId().toString(), contentFont);
            addPDFTableCell(table, enrollment.getCourseTimeId().toString(), contentFont);
            addPDFTableCell(table, enrollment.getScore() != null ? enrollment.getScore().toString() : "", contentFont);
            addPDFTableCell(table, formatInstant(enrollment.getCompletedAt()), contentFont);
            addPDFTableCell(table, formatInstant(enrollment.getEnrolledAt()), contentFont);
        }

        document.add(table);
    }

    private void exportEngagementReportPDF(Long tenantId, ReportPeriod period, Document document,
                                            Font headerFont, Font contentFont) throws DocumentException {
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2, 3, 1, 1, 1, 1.5f});

        addPDFTableHeader(table, new String[]{"사용자ID", "이름", "이메일", "총수강", "완료", "진행중", "평균진도율(%)"}, headerFont);

        List<User> users = getUsersForExport(tenantId, period);
        for (User user : users) {
            long[] stats = getUserEngagementStats(user.getId(), tenantId);
            addPDFTableCell(table, user.getId().toString(), contentFont);
            addPDFTableCell(table, user.getName(), contentFont);
            addPDFTableCell(table, user.getEmail(), contentFont);
            addPDFTableCell(table, String.valueOf(stats[0]), contentFont);
            addPDFTableCell(table, String.valueOf(stats[1]), contentFont);
            addPDFTableCell(table, String.valueOf(stats[2]), contentFont);
            addPDFTableCell(table, String.format("%.1f", (double) stats[3] / 10), contentFont);
        }

        document.add(table);
    }

    private void addPDFTableHeader(PdfPTable table, String[] headers, Font headerFont) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(230, 230, 230));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addPDFTableCell(PdfPTable table, String value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value : "", font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    // ============================================
    // Helper Methods (Data Retrieval)
    // ============================================

    private List<User> getUsersForExport(Long tenantId, ReportPeriod period) {
        return userRepository.findByTenantIdWithPeriodFilter(
                tenantId, period.getStartDate(),
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }

    private List<Course> getCoursesForExport(Long tenantId, ReportPeriod period) {
        var courses = courseRepository.findByTenantId(tenantId,
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();

        if (period.getStartDate() == null) {
            return courses;
        }

        return courses.stream()
                .filter(c -> c.getCreatedAt() == null || !c.getCreatedAt().isBefore(period.getStartDate()))
                .toList();
    }

    private List<Enrollment> getEnrollmentsForExport(Long tenantId, ReportPeriod period) {
        return enrollmentRepository.findByTenantIdWithPeriodFilter(
                tenantId, period.getStartDate(),
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "enrolledAt"))
        ).getContent();
    }

    private List<Enrollment> getCompletedEnrollmentsForExport(Long tenantId, ReportPeriod period) {
        return enrollmentRepository.findCompletedByTenantIdWithPeriodFilter(
                tenantId, period.getStartDate(),
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "completedAt"))
        ).getContent();
    }

    /**
     * 사용자 참여 통계 조회
     * @return [totalEnrollments, completed, enrolled, avgProgress * 10]
     */
    private long[] getUserEngagementStats(Long userId, Long tenantId) {
        long totalEnrollments = enrollmentRepository.countByUserIdAndTenantId(userId, tenantId);
        long completed = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, com.mzc.lp.domain.student.constant.EnrollmentStatus.COMPLETED, tenantId);
        long enrolled = enrollmentRepository.countByUserIdAndStatusAndTenantId(
                userId, com.mzc.lp.domain.student.constant.EnrollmentStatus.ENROLLED, tenantId);
        Double avgProgress = enrollmentRepository.findAverageProgressByUserId(userId, tenantId);
        return new long[]{totalEnrollments, completed, enrolled, avgProgress != null ? (long) (avgProgress * 10) : 0};
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return DATE_FORMATTER.format(instant);
    }
}

package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.certificate.entity.Certificate;
import com.mzc.lp.domain.tenant.entity.TenantSettings;
import com.mzc.lp.domain.tenant.repository.TenantSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificatePdfServiceImpl implements CertificatePdfService {

    private final TenantSettingsRepository tenantSettingsRepository;
    private final ResourceLoader resourceLoader;

    private static final String REGULAR_FONT_PATH = "classpath:fonts/NotoSansKR-Regular.ttf";
    private static final String BOLD_FONT_PATH = "classpath:fonts/NotoSansKR-Bold.ttf";

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일").withZone(ZoneId.of("Asia/Seoul"));

    @Override
    public byte[] generatePdf(Certificate certificate) {
        Long tenantId = TenantContext.getCurrentTenantId();
        TenantSettings settings = tenantSettingsRepository.findByTenantId(tenantId).orElse(null);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // 폰트 로드
            PDType0Font regularFont = loadFont(document, REGULAR_FONT_PATH);
            PDType0Font boldFont = loadFont(document, BOLD_FONT_PATH);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // 1. 배경 그리기
                drawBackground(contentStream, settings);

                // 2. 제목 (수료증)
                drawTitle(contentStream, boldFont);

                // 3. 수료자 이름
                drawUserName(contentStream, boldFont, certificate.getUserName());

                // 4. 본문 (과정명 및 수료 문구)
                drawBody(contentStream, regularFont, boldFont, certificate);

                // 5. 수료일
                drawCompletionDate(contentStream, regularFont, certificate);

                // 6. 발급일
                drawIssuedDate(contentStream, regularFont, certificate);

                // 7. 수료증 번호
                drawCertificateNumber(contentStream, regularFont, certificate);

                // 8. 테두리
                drawBorder(contentStream, settings);
            }

            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate certificate PDF: certificateId={}", certificate.getId(), e);
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    private PDType0Font loadFont(PDDocument document, String path) throws IOException {
        Resource resource = resourceLoader.getResource(path);
        try (InputStream is = resource.getInputStream()) {
            return PDType0Font.load(document, is);
        }
    }

    private void drawBackground(PDPageContentStream contentStream, TenantSettings settings) throws IOException {
        String colorHex = settings != null && settings.getPrimaryColor() != null
                ? settings.getPrimaryColor()
                : "#3B82F6";
        Color accentColor = Color.decode(colorHex);

        // 상단 배너
        contentStream.setNonStrokingColor(accentColor);
        contentStream.addRect(0, PAGE_HEIGHT - 80, PAGE_WIDTH, 80);
        contentStream.fill();

        // 하단 배너
        contentStream.addRect(0, 0, PAGE_WIDTH, 40);
        contentStream.fill();

        // 메인 배경 (흰색)
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.addRect(40, 50, PAGE_WIDTH - 80, PAGE_HEIGHT - 180);
        contentStream.fill();
    }

    private void drawTitle(PDPageContentStream contentStream, PDType0Font boldFont) throws IOException {
        String title = "수 료 증";
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.beginText();
        contentStream.setFont(boldFont, 42);

        float titleWidth = boldFont.getStringWidth(title) / 1000 * 42;
        contentStream.newLineAtOffset((PAGE_WIDTH - titleWidth) / 2, PAGE_HEIGHT - 160);
        contentStream.showText(title);
        contentStream.endText();
    }

    private void drawUserName(PDPageContentStream contentStream, PDType0Font boldFont, String userName) throws IOException {
        contentStream.setNonStrokingColor(Color.BLACK);

        // 사용자 이름
        contentStream.beginText();
        contentStream.setFont(boldFont, 32);
        float nameWidth = boldFont.getStringWidth(userName) / 1000 * 32;
        contentStream.newLineAtOffset((PAGE_WIDTH - nameWidth - 30) / 2, PAGE_HEIGHT - 260);
        contentStream.showText(userName);
        contentStream.endText();

        // "님" 추가
        contentStream.beginText();
        contentStream.setFont(boldFont, 20);
        contentStream.newLineAtOffset((PAGE_WIDTH + nameWidth - 30) / 2 + 10, PAGE_HEIGHT - 260);
        contentStream.showText("님");
        contentStream.endText();

        // 밑줄
        contentStream.setStrokingColor(Color.BLACK);
        contentStream.setLineWidth(1);
        float underlineStart = (PAGE_WIDTH - nameWidth - 60) / 2;
        contentStream.moveTo(underlineStart, PAGE_HEIGHT - 268);
        contentStream.lineTo(underlineStart + nameWidth + 60, PAGE_HEIGHT - 268);
        contentStream.stroke();
    }

    private void drawBody(PDPageContentStream contentStream, PDType0Font regularFont,
                          PDType0Font boldFont, Certificate certificate) throws IOException {
        String courseName = certificate.getCourseTimeTitle();

        // "위 사람은"
        contentStream.beginText();
        contentStream.setFont(regularFont, 16);
        String prefix = "위 사람은";
        float prefixWidth = regularFont.getStringWidth(prefix) / 1000 * 16;
        contentStream.newLineAtOffset((PAGE_WIDTH - prefixWidth) / 2, PAGE_HEIGHT - 340);
        contentStream.showText(prefix);
        contentStream.endText();

        // 과정명
        contentStream.beginText();
        contentStream.setFont(boldFont, 20);
        float courseWidth = boldFont.getStringWidth(courseName) / 1000 * 20;
        float courseX = (PAGE_WIDTH - courseWidth) / 2;
        if (courseX < 60) courseX = 60;
        contentStream.newLineAtOffset(courseX, PAGE_HEIGHT - 390);
        contentStream.showText(courseName);
        contentStream.endText();

        // "과정을 성실히 이수하였기에"
        contentStream.beginText();
        contentStream.setFont(regularFont, 16);
        String middle = "과정을 성실히 이수하였기에";
        float middleWidth = regularFont.getStringWidth(middle) / 1000 * 16;
        contentStream.newLineAtOffset((PAGE_WIDTH - middleWidth) / 2, PAGE_HEIGHT - 440);
        contentStream.showText(middle);
        contentStream.endText();

        // "이 증서를 수여합니다."
        contentStream.beginText();
        contentStream.setFont(regularFont, 16);
        String suffix = "이 증서를 수여합니다.";
        float suffixWidth = regularFont.getStringWidth(suffix) / 1000 * 16;
        contentStream.newLineAtOffset((PAGE_WIDTH - suffixWidth) / 2, PAGE_HEIGHT - 470);
        contentStream.showText(suffix);
        contentStream.endText();
    }

    private void drawCompletionDate(PDPageContentStream contentStream, PDType0Font regularFont,
                                    Certificate certificate) throws IOException {
        String completedDate = DATE_FORMATTER.format(certificate.getCompletedAt());
        String text = "수료일: " + completedDate;

        contentStream.beginText();
        contentStream.setFont(regularFont, 14);
        contentStream.setNonStrokingColor(Color.DARK_GRAY);
        float textWidth = regularFont.getStringWidth(text) / 1000 * 14;
        contentStream.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, PAGE_HEIGHT - 550);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawIssuedDate(PDPageContentStream contentStream, PDType0Font regularFont,
                                Certificate certificate) throws IOException {
        String issuedDate = DATE_FORMATTER.format(certificate.getIssuedAt());
        String text = "발급일: " + issuedDate;

        contentStream.beginText();
        contentStream.setFont(regularFont, 14);
        contentStream.setNonStrokingColor(Color.DARK_GRAY);
        float textWidth = regularFont.getStringWidth(text) / 1000 * 14;
        contentStream.newLineAtOffset((PAGE_WIDTH - textWidth) / 2, PAGE_HEIGHT - 580);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawCertificateNumber(PDPageContentStream contentStream, PDType0Font regularFont,
                                       Certificate certificate) throws IOException {
        String text = "수료증 번호: " + certificate.getCertificateNumber();

        contentStream.beginText();
        contentStream.setFont(regularFont, 10);
        contentStream.setNonStrokingColor(Color.GRAY);
        contentStream.newLineAtOffset(60, 60);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawBorder(PDPageContentStream contentStream, TenantSettings settings) throws IOException {
        String colorHex = settings != null && settings.getSecondaryColor() != null
                ? settings.getSecondaryColor()
                : "#1E40AF";
        Color borderColor = Color.decode(colorHex);

        contentStream.setStrokingColor(borderColor);
        contentStream.setLineWidth(3);
        contentStream.addRect(30, 30, PAGE_WIDTH - 60, PAGE_HEIGHT - 60);
        contentStream.stroke();

        // 내부 테두리 (골드)
        contentStream.setStrokingColor(new Color(212, 175, 55));
        contentStream.setLineWidth(1);
        contentStream.addRect(35, 35, PAGE_WIDTH - 70, PAGE_HEIGHT - 70);
        contentStream.stroke();
    }
}

package com.mzc.lp.domain.content.service;
import com.mzc.lp.common.support.TenantTestSupport;

import com.mzc.lp.domain.content.constant.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ThumbnailServiceTest extends TenantTestSupport {

    private ThumbnailService thumbnailService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        thumbnailService = new ThumbnailServiceImpl(tempDir.toString(), "ffmpeg");
    }

    @Nested
    @DisplayName("이미지 썸네일 생성")
    class GenerateImageThumbnail {

        @Test
        @DisplayName("성공 - JPG 이미지 썸네일 생성")
        void generateImageThumbnail_success_jpg() throws Exception {
            // given
            Path imagePath = createTestImage("test.jpg", "jpg", 800, 600);

            // when
            Optional<String> result = thumbnailService.generateImageThumbnail(imagePath);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).contains("/uploads/thumbnails/");
            assertThat(result.get()).endsWith(".jpg");
        }

        @Test
        @DisplayName("성공 - PNG 이미지 썸네일 생성")
        void generateImageThumbnail_success_png() throws Exception {
            // given
            Path imagePath = createTestImage("test.png", "png", 1920, 1080);

            // when
            Optional<String> result = thumbnailService.generateImageThumbnail(imagePath);

            // then
            assertThat(result).isPresent();
            assertThat(result.get()).contains("/uploads/thumbnails/");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 파일")
        void generateImageThumbnail_fail_fileNotFound() {
            // given
            Path nonExistentPath = tempDir.resolve("non-existent.jpg");

            // when
            Optional<String> result = thumbnailService.generateImageThumbnail(nonExistentPath);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("콘텐츠 타입별 썸네일 생성")
    class GenerateThumbnail {

        @Test
        @DisplayName("IMAGE 타입 - 썸네일 생성")
        void generateThumbnail_image() throws Exception {
            // given
            Path imagePath = createTestImage("test.jpg", "jpg", 640, 480);

            // when
            Optional<String> result = thumbnailService.generateThumbnail(imagePath, ContentType.IMAGE);

            // then
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("EXTERNAL_LINK 타입 - 썸네일 미생성")
        void generateThumbnail_externalLink() throws Exception {
            // given
            Path dummyPath = tempDir.resolve("dummy.txt");
            Files.writeString(dummyPath, "dummy content");

            // when
            Optional<String> result = thumbnailService.generateThumbnail(dummyPath, ContentType.EXTERNAL_LINK);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("AUDIO 타입 - 썸네일 미생성")
        void generateThumbnail_audio() throws Exception {
            // given
            Path dummyPath = tempDir.resolve("audio.mp3");
            Files.writeString(dummyPath, "dummy audio content");

            // when
            Optional<String> result = thumbnailService.generateThumbnail(dummyPath, ContentType.AUDIO);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("DOCUMENT (non-PDF) - 썸네일 미생성")
        void generateThumbnail_document_nonPdf() throws Exception {
            // given
            Path docPath = tempDir.resolve("document.docx");
            Files.writeString(docPath, "dummy document content");

            // when
            Optional<String> result = thumbnailService.generateThumbnail(docPath, ContentType.DOCUMENT);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("썸네일 삭제")
    class DeleteThumbnail {

        @Test
        @DisplayName("성공 - 썸네일 삭제")
        void deleteThumbnail_success() throws Exception {
            // given
            Path imagePath = createTestImage("test.jpg", "jpg", 320, 180);
            Optional<String> thumbnailPath = thumbnailService.generateImageThumbnail(imagePath);
            assertThat(thumbnailPath).isPresent();

            // when
            thumbnailService.deleteThumbnail(thumbnailPath.get());

            // then - 삭제 후 에러 없이 완료되면 성공
        }

        @Test
        @DisplayName("null 경로 - 에러 없이 무시")
        void deleteThumbnail_nullPath() {
            // when & then - 에러 없이 완료
            thumbnailService.deleteThumbnail(null);
        }

        @Test
        @DisplayName("빈 경로 - 에러 없이 무시")
        void deleteThumbnail_emptyPath() {
            // when & then - 에러 없이 완료
            thumbnailService.deleteThumbnail("");
        }
    }

    // 테스트용 이미지 파일 생성 헬퍼
    private Path createTestImage(String fileName, String format, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Test Image", 10, 30);
        g2d.dispose();

        Path imagePath = tempDir.resolve(fileName);
        ImageIO.write(image, format, imagePath.toFile());
        return imagePath;
    }
}

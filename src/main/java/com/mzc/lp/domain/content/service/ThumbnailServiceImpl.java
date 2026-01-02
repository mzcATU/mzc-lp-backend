package com.mzc.lp.domain.content.service;

import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.exception.FileStorageException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ThumbnailServiceImpl implements ThumbnailService {

    private static final int THUMBNAIL_WIDTH = 320;
    private static final int THUMBNAIL_HEIGHT = 180;
    private static final String THUMBNAIL_FORMAT = "jpg";

    private final Path thumbnailPath;
    private final String ffmpegPath;

    public ThumbnailServiceImpl(
            @Value("${file.upload-dir:./uploads}") String uploadDir,
            @Value("${thumbnail.ffmpeg-path:ffmpeg}") String ffmpegPath) {
        this.thumbnailPath = Paths.get(uploadDir).resolve("thumbnails").toAbsolutePath().normalize();
        this.ffmpegPath = ffmpegPath;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.thumbnailPath);
            log.info("Thumbnail storage initialized at: {}", this.thumbnailPath);
        } catch (IOException e) {
            log.error("Could not create thumbnail directory", e);
        }
    }

    @Override
    public Optional<String> generateThumbnail(Path sourcePath, ContentType contentType) {
        return switch (contentType) {
            case VIDEO -> generateVideoThumbnail(sourcePath);
            case IMAGE -> generateImageThumbnail(sourcePath);
            case DOCUMENT -> {
                String extension = getFileExtension(sourcePath.getFileName().toString());
                if ("pdf".equalsIgnoreCase(extension)) {
                    yield generatePdfThumbnail(sourcePath);
                }
                yield Optional.empty();
            }
            default -> Optional.empty();
        };
    }

    @Override
    public Optional<String> generateVideoThumbnail(Path videoPath) {
        Process process = null;
        try {
            String outputFileName = generateThumbnailFileName();
            Path outputPath = getThumbnailOutputPath(outputFileName);

            // ffmpeg -i input.mp4 -ss 00:00:01 -vframes 1 -vf "scale=320:180" output.jpg
            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-i", videoPath.toString(),
                    "-ss", "00:00:01",
                    "-vframes", "1",
                    "-vf", String.format("scale=%d:%d:force_original_aspect_ratio=decrease,pad=%d:%d:(ow-iw)/2:(oh-ih)/2",
                            THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT),
                    "-y",
                    outputPath.toString()
            );
            pb.redirectErrorStream(true);

            log.info("Executing FFmpeg command: {} -i {} ...", ffmpegPath, videoPath);
            process = pb.start();

            // FFmpeg 출력 읽기
            String output = new String(process.getInputStream().readAllBytes());

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (finished && process.exitValue() == 0 && Files.exists(outputPath)) {
                String relativePath = "/uploads/thumbnails/" + getDatePath() + "/" + outputFileName;
                log.info("Video thumbnail generated: {}", relativePath);
                return Optional.of(relativePath);
            } else {
                log.warn("Failed to generate video thumbnail for: {}. Exit code: {}, Output: {}",
                        videoPath, process.exitValue(), output);
                return Optional.empty();
            }
        } catch (IOException e) {
            log.error("IO error generating video thumbnail: {}", videoPath, e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Video thumbnail generation interrupted: {}", videoPath, e);
            return Optional.empty();
        } finally {
            // Process 리소스 정리
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    @Override
    public Optional<String> generateImageThumbnail(Path imagePath) {
        try {
            BufferedImage originalImage = ImageIO.read(imagePath.toFile());
            if (originalImage == null) {
                log.warn("Could not read image: {}", imagePath);
                return Optional.empty();
            }

            BufferedImage thumbnail = resizeImage(originalImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

            String outputFileName = generateThumbnailFileName();
            Path outputPath = getThumbnailOutputPath(outputFileName);

            ImageIO.write(thumbnail, THUMBNAIL_FORMAT, outputPath.toFile());

            String relativePath = "/uploads/thumbnails/" + getDatePath() + "/" + outputFileName;
            log.info("Image thumbnail generated: {}", relativePath);
            return Optional.of(relativePath);
        } catch (Exception e) {
            log.error("Error generating image thumbnail: {}", imagePath, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> generatePdfThumbnail(Path pdfPath) {
        try (PDDocument document = Loader.loadPDF(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage pageImage = renderer.renderImageWithDPI(0, 72);

            BufferedImage thumbnail = resizeImage(pageImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);

            String outputFileName = generateThumbnailFileName();
            Path outputPath = getThumbnailOutputPath(outputFileName);

            ImageIO.write(thumbnail, THUMBNAIL_FORMAT, outputPath.toFile());

            String relativePath = "/uploads/thumbnails/" + getDatePath() + "/" + outputFileName;
            log.info("PDF thumbnail generated: {}", relativePath);
            return Optional.of(relativePath);
        } catch (Exception e) {
            log.error("Error generating PDF thumbnail: {}", pdfPath, e);
            return Optional.empty();
        }
    }

    @Override
    public String storeCustomThumbnail(MultipartFile thumbnail) {
        try {
            String originalFileName = thumbnail.getOriginalFilename();
            String extension = getFileExtension(originalFileName != null ? originalFileName : "image.jpg");

            // jpg, jpeg, png만 허용
            if (!extension.matches("(?i)jpg|jpeg|png")) {
                extension = "jpg";
            }

            String outputFileName = UUID.randomUUID().toString() + "." + extension;
            Path outputPath = getThumbnailOutputPath(outputFileName);

            // 이미지 리사이즈 후 저장
            BufferedImage originalImage = ImageIO.read(thumbnail.getInputStream());
            if (originalImage != null) {
                BufferedImage resizedImage = resizeImage(originalImage, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
                ImageIO.write(resizedImage, extension.equalsIgnoreCase("png") ? "png" : "jpg", outputPath.toFile());
            } else {
                // 이미지 읽기 실패 시 원본 그대로 저장
                Files.copy(thumbnail.getInputStream(), outputPath);
            }

            String relativePath = "/uploads/thumbnails/" + getDatePath() + "/" + outputFileName;
            log.info("Custom thumbnail stored: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            log.error("Failed to store custom thumbnail", e);
            throw new FileStorageException("Failed to store custom thumbnail: " + e.getMessage());
        }
    }

    @Override
    public void deleteThumbnail(String thumbnailPathStr) {
        if (thumbnailPathStr == null || thumbnailPathStr.isBlank()) {
            return;
        }

        try {
            String relativePath = thumbnailPathStr.startsWith("/uploads/thumbnails/")
                    ? thumbnailPathStr.substring("/uploads/thumbnails/".length())
                    : thumbnailPathStr;

            Path file = this.thumbnailPath.resolve(relativePath).normalize();
            Files.deleteIfExists(file);
            log.info("Thumbnail deleted: {}", thumbnailPathStr);
        } catch (IOException e) {
            log.error("Failed to delete thumbnail: {}", thumbnailPathStr, e);
        }
    }

    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        double aspectRatio = (double) original.getWidth() / original.getHeight();
        double targetRatio = (double) targetWidth / targetHeight;

        int width, height;
        if (aspectRatio > targetRatio) {
            width = targetWidth;
            height = (int) (targetWidth / aspectRatio);
        } else {
            height = targetHeight;
            width = (int) (targetHeight * aspectRatio);
        }

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (targetWidth - width) / 2;
        int y = (targetHeight - height) / 2;
        g2d.drawImage(original, x, y, width, height, null);
        g2d.dispose();

        return resized;
    }

    private String generateThumbnailFileName() {
        return UUID.randomUUID().toString() + "." + THUMBNAIL_FORMAT;
    }

    private String getDatePath() {
        LocalDate now = LocalDate.now();
        return String.format("%d/%02d", now.getYear(), now.getMonthValue());
    }

    private Path getThumbnailOutputPath(String fileName) throws IOException {
        Path datePath = this.thumbnailPath.resolve(getDatePath());
        Files.createDirectories(datePath);
        return datePath.resolve(fileName);
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}

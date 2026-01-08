package com.mzc.lp.domain.content.service;

import com.mzc.lp.domain.content.constant.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 미디어 파일의 메타데이터를 추출하는 서비스 구현체
 * - 비디오/오디오: ffprobe를 사용하여 duration 추출
 * - PDF: PDFBox를 사용하여 pageCount 추출
 */
@Slf4j
@Service
public class MediaMetadataServiceImpl implements MediaMetadataService {

    private final String ffprobePath;

    public MediaMetadataServiceImpl(
            @Value("${thumbnail.ffprobe-path:ffprobe}") String ffprobePath) {
        this.ffprobePath = ffprobePath;
    }

    @Override
    public Optional<Integer> extractDuration(Path filePath) {
        if (!Files.exists(filePath)) {
            log.warn("File does not exist: {}", filePath);
            return Optional.empty();
        }

        Process process = null;
        try {
            // ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 input.mp4
            ProcessBuilder pb = new ProcessBuilder(
                    ffprobePath,
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    filePath.toString()
            );
            pb.redirectErrorStream(true);

            log.debug("Executing ffprobe for duration: {}", filePath);
            process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (finished && process.exitValue() == 0) {
                String durationStr = output.toString().trim();
                if (!durationStr.isEmpty()) {
                    double durationSeconds = Double.parseDouble(durationStr);
                    int duration = (int) Math.round(durationSeconds);
                    log.info("Extracted duration: {} seconds from {}", duration, filePath.getFileName());
                    return Optional.of(duration);
                }
            } else {
                log.warn("ffprobe failed for: {}. Exit code: {}", filePath, process.exitValue());
            }
        } catch (NumberFormatException e) {
            log.error("Failed to parse duration from ffprobe output: {}", filePath, e);
        } catch (Exception e) {
            log.error("Error extracting duration: {}", filePath, e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<Integer> extractPageCount(Path filePath) {
        if (!Files.exists(filePath)) {
            log.warn("File does not exist: {}", filePath);
            return Optional.empty();
        }

        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            int pageCount = document.getNumberOfPages();
            log.info("Extracted page count: {} pages from {}", pageCount, filePath.getFileName());
            return Optional.of(pageCount);
        } catch (Exception e) {
            log.error("Error extracting page count: {}", filePath, e);
            return Optional.empty();
        }
    }

    /**
     * 비디오 해상도 추출 (예: 1920x1080)
     */
    private Optional<String> extractResolution(Path filePath) {
        Process process = null;
        try {
            // ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 input.mp4
            ProcessBuilder pb = new ProcessBuilder(
                    ffprobePath,
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height",
                    "-of", "csv=s=x:p=0",
                    filePath.toString()
            );
            pb.redirectErrorStream(true);

            process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (finished && process.exitValue() == 0) {
                String resolution = output.toString().trim();
                // 형식: 1920x1080
                if (resolution.matches("\\d+x\\d+")) {
                    log.info("Extracted resolution: {} from {}", resolution, filePath.getFileName());
                    return Optional.of(resolution);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting resolution: {}", filePath, e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }

        return Optional.empty();
    }

    @Override
    public MediaMetadata extractMetadata(Path filePath, ContentType contentType) {
        return switch (contentType) {
            case VIDEO -> {
                Integer duration = extractDuration(filePath).orElse(null);
                String resolution = extractResolution(filePath).orElse(null);
                yield MediaMetadata.ofDuration(duration, resolution);
            }
            case AUDIO -> {
                Integer duration = extractDuration(filePath).orElse(null);
                yield MediaMetadata.ofDuration(duration, null);
            }
            case DOCUMENT -> {
                String extension = getFileExtension(filePath.getFileName().toString());
                if ("pdf".equalsIgnoreCase(extension)) {
                    Integer pageCount = extractPageCount(filePath).orElse(null);
                    yield MediaMetadata.ofPageCount(pageCount);
                }
                yield MediaMetadata.empty();
            }
            default -> MediaMetadata.empty();
        };
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}

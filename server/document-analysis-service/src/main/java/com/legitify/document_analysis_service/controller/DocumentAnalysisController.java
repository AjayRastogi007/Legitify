package com.legitify.document_analysis_service.controller;

import com.legitify.document_analysis_service.dto.AnalysisJobDto;
import com.legitify.document_analysis_service.entity.AnalysisJob;
import com.legitify.document_analysis_service.repository.AnalysisJobRepository;
import com.legitify.document_analysis_service.utils.ExtractionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.legitify.document_analysis_service.service.DocumentAnalysisService;
import com.legitify.document_analysis_service.service.GeminiService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/legitify/service")
public class DocumentAnalysisController {

    private final DocumentAnalysisService documentService;
    private final GeminiService geminiService;
    private final AnalysisJobRepository jobRepository;
    private final ExecutorService executor;

    private static final Logger log = LoggerFactory.getLogger(DocumentAnalysisController.class);

    public DocumentAnalysisController(DocumentAnalysisService documentService, GeminiService geminiService, AnalysisJobRepository jobRepository, ExecutorService executor) {
        this.documentService = documentService;
        this.geminiService = geminiService;
        this.jobRepository = jobRepository;
        this.executor = executor;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeAsync(@RequestParam("file") MultipartFile file) {

        String jobId = UUID.randomUUID().toString();

        AnalysisJob job = new AnalysisJob();
        job.setId(jobId);
        job.setStatus(AnalysisJob.Status.PENDING);
        jobRepository.save(job);

        Path tempFile;

        try {
            String original = Objects.requireNonNull(file.getOriginalFilename()).toLowerCase(java.util.Locale.ROOT);
            String ext;
            if (original.endsWith(".pdf")) ext = ".pdf";
            else if (original.endsWith(".docx")) ext = ".docx";
            else if (original.endsWith(".txt")) ext = ".txt";
            else ext = ".tmp";
            tempFile = Files.createTempFile("upload-", ext);
            file.transferTo(tempFile.toFile());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }

        final Path tempFileFinal = tempFile;
        executor.submit(() -> {
            log.info("[{}] Async task started, tempFile={}", jobId, tempFileFinal);
            try {
                try {
                    job.setStatus(AnalysisJob.Status.PROCESSING);
                    jobRepository.save(job);
                    log.info("[{}] Status -> PROCESSING", jobId);
                } catch (Throwable t) {
                    log.error("[{}] Failed to persist PROCESSING status", jobId, t);
                }

                log.info("[{}] Extracting text", jobId);
                ExtractionResult extractionResult = documentService.getTextFromPath(tempFileFinal);
                log.info("[{}] Extracted: pages={}, fullText length={}, errors={}, warnings={}",
                        jobId,
                        extractionResult.pages.size(),
                        extractionResult.fullText() == null ? 0 : extractionResult.fullText().length(),
                        extractionResult.errors,
                        extractionResult.warnings);

                log.info("[{}] Calling Gemini", jobId);
                String jsonResponse = geminiService.analyzeDocument(extractionResult);
                log.info("[{}] Gemini returned. JSON length={}",
                        jobId, jsonResponse == null ? -1 : jsonResponse.length());

                log.info("[{}] Generating PDF", jobId);
                String pdfPath = documentService.generatePdfFromString(jsonResponse);
                log.info("[{}] PDF generated at {}", jobId, pdfPath);

                String fileName = new File(pdfPath).getName();
                job.setPdfUrl("/legitify/service/pdf/" + fileName);
                job.setStatus(AnalysisJob.Status.DONE);
                jobRepository.save(job);
                log.info("[{}] Status -> DONE, pdfUrl={}", jobId, job.getPdfUrl());

                try { Files.deleteIfExists(tempFileFinal); } catch (Throwable ignored) {}

            } catch (Throwable t) {
                log.error("[{}] Async task FAILED", jobId, t);
                try {
                    job.setStatus(AnalysisJob.Status.FAILED);
                    job.setError(t.getClass().getSimpleName() + ": " + t.getMessage());
                    jobRepository.save(job);
                    log.info("[{}] Status -> FAILED", jobId);
                } catch (Throwable inner) {
                    log.error("[{}] CRITICAL: Could not even mark job FAILED", jobId, inner);
                }
            }
        });

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", jobId));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<?> getJob(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
                .map(job -> ResponseEntity.ok(
                        new AnalysisJobDto(
                                job.getStatus().name(),
                                job.getPdfUrl(),
                                job.getError()
                        )
                ))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pdf/{fileName}")
    public ResponseEntity<Resource> servePdf(@PathVariable String fileName) {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path pdfPath = tempDir.resolve(fileName).normalize();

        if (!pdfPath.startsWith(tempDir)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        File file = pdfPath.toFile();
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"").body(resource);
    }

}

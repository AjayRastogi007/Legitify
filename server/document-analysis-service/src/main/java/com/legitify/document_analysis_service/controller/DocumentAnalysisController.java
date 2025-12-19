package com.legitify.document_analysis_service.controller;

import com.legitify.document_analysis_service.entity.AnalysisJob;
import com.legitify.document_analysis_service.repository.AnalysisJobRepository;
import com.legitify.document_analysis_service.utils.ExtractionResult;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@RestController
@RequestMapping("/legitify/service")
public class DocumentAnalysisController {

    private final DocumentAnalysisService documentService;
    private final GeminiService geminiService;
    private final AnalysisJobRepository jobRepository;
    private final ExecutorService executor;

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

        executor.submit(() -> {
            try {
                job.setStatus(AnalysisJob.Status.PROCESSING);
                jobRepository.save(job);

                ExtractionResult extractionResult = documentService.getTextFromFile(file);
                String jsonResponse = geminiService.analyzeDocument(extractionResult);
                String pdfPath = documentService.generatePdfFromString(jsonResponse);

                String fileName = new File(pdfPath).getName();
                job.setPdfUrl("/legitify/service/pdf/" + fileName);
                job.setStatus(AnalysisJob.Status.DONE);
                jobRepository.save(job);

            } catch (Exception e) {
                job.setStatus(AnalysisJob.Status.FAILED);
                job.setError(e.getMessage());
                jobRepository.save(job);
            }
        });

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("jobId", jobId));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<?> getJob(@PathVariable String jobId) {
        return jobRepository.findById(jobId)
                .map(job -> ResponseEntity.ok(Map.of(
                        "status", job.getStatus(),
                        "pdfUrl", job.getPdfUrl(),
                        "error", job.getError()
                )))
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

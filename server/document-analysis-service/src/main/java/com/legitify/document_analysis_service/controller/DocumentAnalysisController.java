package com.legitify.document_analysis_service.controller;

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
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/legitify/service")
public class DocumentAnalysisController {

    private final DocumentAnalysisService documentService;
    private final GeminiService geminiService;

    public DocumentAnalysisController(DocumentAnalysisService documentService, GeminiService geminiService) {
        this.documentService = documentService;
        this.geminiService = geminiService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeAndGenerate(@RequestParam("file") MultipartFile file) {
        ExtractionResult extractionResult = documentService.getTextFromFile(file);
        String jsonResponse = geminiService.analyzeDocument(extractionResult);
        String pdfPath = documentService.generatePdfFromString(jsonResponse);

        String fileName = new File(pdfPath).getName();
        Map<String, String> response = new HashMap<>();
        response.put("pdfUrl", "/service/pdf/" + fileName);
        return ResponseEntity.ok(response);
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

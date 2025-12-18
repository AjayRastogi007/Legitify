package com.legitify.document_analysis_service.service;

import com.legitify.document_analysis_service.utils.ExtractionResult;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentAnalysisService {
    ExtractionResult getTextFromFile(MultipartFile file);
    String generatePdfFromString(String jsonResponse);
}

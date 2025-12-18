package com.legitify.document_analysis_service.service;

import com.legitify.document_analysis_service.utils.ExtractionResult;

public interface GeminiService {
    String analyzeDocument(ExtractionResult extractionResult);
}

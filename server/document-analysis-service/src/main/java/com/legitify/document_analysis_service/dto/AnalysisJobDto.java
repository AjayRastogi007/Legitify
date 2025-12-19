package com.legitify.document_analysis_service.dto;

public record AnalysisJobDto(
        String status,
        String pdfUrl,
        String error
) {}

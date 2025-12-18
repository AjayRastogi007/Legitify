package com.legitify.document_analysis_service.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.legitify.document_analysis_service.utils.DocumentAnalyzer;
import com.legitify.document_analysis_service.utils.ExtractionResult;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legitify.document_analysis_service.service.GeminiService;
import com.legitify.document_analysis_service.utils.DocumentAnalysisUtils;

@Service
public class GeminiServiceImpl implements GeminiService {

    private final DocumentAnalyzer analyzer;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiServiceImpl() {
        GoogleAiGeminiChatModel model =
                GoogleAiGeminiChatModel.builder()
                        .apiKey(System.getenv("GEMINI_API_KEY"))
                        .modelName("gemini-2.5-flash")
                        .temperature(0.2)
                        .build();

        this.analyzer = AiServices.create(
                DocumentAnalyzer.class,
                model
        );
    }

    @Override
    public String analyzeDocument(ExtractionResult extractionResult) {
        if (!extractionResult.errors.isEmpty()) {
            return errorJson("Extraction failed", extractionResult.errors);
        }

        if (extractionResult.pages.isEmpty()
                && extractionResult.fullText().isBlank()) {
            return errorJson("No text to analyze", null);
        }

        List<JsonNode> results = new ArrayList<>();

        String combinedText = extractionResult.fullText();

        try {
            String response = analyzer.analyze(combinedText, -1);
            String cleanJson = extractJson(response);

            JsonNode result = mapper.readTree(cleanJson);

            return DocumentAnalysisUtils.mergeResponses(
                    List.of(result),
                    extractionResult.metadata,
                    extractionResult.warnings,
                    extractionResult.ocrSuggested
            );

        } catch (Exception e) {
            return errorJson("AI analysis failed", List.of(e.getMessage()));
        }
    }

    private String errorJson(String message, List<String> details) {
        try {
            var node = mapper.createObjectNode();
            node.put("error", message);
            if (details != null)
                node.set("details", mapper.valueToTree(details));
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{\"error\":\"" + message + "\"}";
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');

        if (start == -1 || end == -1 || start > end) {
            throw new IllegalArgumentException("No valid JSON object found");
        }

        return raw.substring(start, end + 1);
    }

}

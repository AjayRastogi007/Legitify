package com.legitify.document_analysis_service.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.legitify.document_analysis_service.utils.DocumentAnalyzer;
import com.legitify.document_analysis_service.utils.ExtractionResult;
import com.legitify.document_analysis_service.utils.Page;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legitify.document_analysis_service.service.GeminiService;
import com.legitify.document_analysis_service.utils.DocumentAnalysisUtils;

@Service
public class GeminiServiceImpl implements GeminiService {

    private final DocumentAnalyzer analyzer;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiServiceImpl(
            @Value("${GEMINI_API_KEY}") String geminiApiKey
    ) {
        GoogleAiGeminiChatModel model =
                GoogleAiGeminiChatModel.builder()
                        .apiKey(geminiApiKey)
                        .modelName("gemini-2.5-flash")
                        .temperature(0.2)
                        .build();

        this.analyzer = AiServices.create(
                DocumentAnalyzer.class,
                model
        );
    }

    private static final int PAGES_PER_BATCH = 8;

    @Override
    public String analyzeDocument(ExtractionResult extractionResult) {
        if (!extractionResult.errors.isEmpty()) {
            return errorJson("Extraction failed", extractionResult.errors);
        }

        List<Page> pages = extractionResult.pages.stream()
                .filter(p -> p.text != null && !p.text.isBlank())
                .toList();

        if (pages.isEmpty() && extractionResult.fullText().isBlank()) {
            return errorJson("No text to analyze", null);
        }

        // Fallback: if no per-page split but we do have fullText, treat it as one page
        if (pages.isEmpty()) {
            pages = List.of(new Page(1, extractionResult.fullText()));
        }

        List<JsonNode> results = new ArrayList<>();
        List<String> warnings = new ArrayList<>(extractionResult.warnings);

        for (int i = 0; i < pages.size(); i += PAGES_PER_BATCH) {
            List<Page> chunk = pages.subList(i, Math.min(i + PAGES_PER_BATCH, pages.size()));

            StringBuilder buf = new StringBuilder();
            for (Page p : chunk) {
                buf.append("<<PAGE_BREAK_").append(p.pageNumber).append(">>\n")
                        .append(p.text).append("\n");
            }

            int firstPage = chunk.getFirst().pageNumber;
            int lastPage = chunk.getLast().pageNumber;

            try {
                String response = analyzer.analyzeBatch(buf.toString());

                System.out.println("RAW GEMINI RESPONSE (Pages " + firstPage + "-" + lastPage + "):\n" + response);

                String cleanJson = extractJson(response);

                System.out.println("CLEAN JSON (Pages " + firstPage + "-" + lastPage + "):\n" + cleanJson);

                results.add(mapper.readTree(cleanJson));
            } catch (dev.langchain4j.exception.RateLimitException rle) {
                warnings.add("Rate limit hit at pages " + firstPage + "-" + lastPage
                        + "; remaining pages skipped. Try again later. (" + rle.getMessage() + ")");
                break; // keep partial results
            } catch (Exception e) {
                warnings.add("Batch pages " + firstPage + "-" + lastPage
                        + " failed: " + e.getMessage());
            }
        }

        if (results.isEmpty()) {
            return errorJson("AI analysis failed", warnings);
        }

        return DocumentAnalysisUtils.mergeResponses(
                results,
                extractionResult.metadata,
                warnings,
                extractionResult.ocrSuggested
        );
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

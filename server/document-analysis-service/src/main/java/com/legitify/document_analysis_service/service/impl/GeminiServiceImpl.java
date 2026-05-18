package com.legitify.document_analysis_service.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.legitify.document_analysis_service.utils.DocumentAnalyzer;
import com.legitify.document_analysis_service.utils.ExtractionResult;
import com.legitify.document_analysis_service.utils.Page;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legitify.document_analysis_service.service.GeminiService;
import com.legitify.document_analysis_service.utils.DocumentAnalysisUtils;

@Service
public class GeminiServiceImpl implements GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiServiceImpl.class);
    private static final int PAGES_PER_BATCH = 8;

    private final DocumentAnalyzer analyzer;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiServiceImpl(@Value("${GEMINI_API_KEY}") String geminiApiKey) {
        GoogleAiGeminiChatModel model = GoogleAiGeminiChatModel.builder().apiKey(geminiApiKey).modelName("gemini-2.5-flash").temperature(0.2).build();

        this.analyzer = AiServices.create(DocumentAnalyzer.class, model);
    }

    @Override
    public String analyzeDocument(ExtractionResult extractionResult) {
        if (!extractionResult.errors.isEmpty()) {
            log.warn("Extraction errors present, aborting analysis: {}", extractionResult.errors);
            return errorJson("Extraction failed", extractionResult.errors);
        }

        List<Page> pages = extractionResult.pages.stream().filter(p -> p.text != null && !p.text.isBlank()).toList();

        if (pages.isEmpty() && extractionResult.fullText().isBlank()) {
            log.warn("No text to analyze");
            return errorJson("No text to analyze", null);
        }

        if (pages.isEmpty()) {
            pages = List.of(new Page(1, extractionResult.fullText()));
        }

        log.info("Analyzing {} pages in batches of {}", pages.size(), PAGES_PER_BATCH);

        List<JsonNode> results = new ArrayList<>();
        List<String> warnings = new ArrayList<>(extractionResult.warnings);

        for (int i = 0; i < pages.size(); i += PAGES_PER_BATCH) {
            List<Page> chunk = pages.subList(i, Math.min(i + PAGES_PER_BATCH, pages.size()));
            int firstPage = chunk.getFirst().pageNumber;
            int lastPage = chunk.getLast().pageNumber;

            StringBuilder buf = new StringBuilder();
            for (Page p : chunk) {
                buf.append("<<PAGE_BREAK_").append(p.pageNumber).append(">>\n").append(p.text).append("\n");
            }

            try {
                log.info("Calling Gemini for pages {}-{} (chars={})", firstPage, lastPage, buf.length());
                String response = analyzer.analyzeBatch(buf.toString());
                log.info("Gemini response received for pages {}-{}, length={}", firstPage, lastPage, response == null ? -1 : response.length());

                String cleanJson = extractJson(response);
                JsonNode node = mapper.readTree(cleanJson);
                results.add(node);
                log.info("Parsed JSON for pages {}-{}: clauses={}, risks={}", firstPage, lastPage, node.path("clauses").size(), node.path("risksSummary").size());

            } catch (dev.langchain4j.exception.RateLimitException rle) {
                log.warn("Rate limit hit at pages {}-{}", firstPage, lastPage);
                warnings.add("Rate limit hit at pages " + firstPage + "-" + lastPage + "; remaining pages skipped.");
                break;
            } catch (Throwable t) {
                log.error("Batch pages {}-{} failed", firstPage, lastPage, t);
                warnings.add("Batch pages " + firstPage + "-" + lastPage + " failed: " + t.getMessage());
            }
        }

        if (results.isEmpty()) {
            log.warn("No successful results, returning error JSON");
            return errorJson("AI analysis failed", warnings);
        }

        try {
            String merged = DocumentAnalysisUtils.mergeResponses(results, extractionResult.metadata, warnings, extractionResult.ocrSuggested);
            log.info("Merged JSON length={}", merged == null ? -1 : merged.length());
            return merged;
        } catch (Throwable t) {
            log.error("mergeResponses failed", t);
            return errorJson("Merge failed", List.of(t.getMessage()));
        }
    }

    private String errorJson(String message, List<String> details) {
        try {
            var node = mapper.createObjectNode();
            node.put("error", message);
            if (details != null) node.set("details", mapper.valueToTree(details));
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{\"error\":\"" + message + "\"}";
        }
    }

    private String extractJson(String raw) {
        if (raw == null) throw new IllegalArgumentException("Null response from model");
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start == -1 || end == -1 || start > end) {
            throw new IllegalArgumentException("No valid JSON object found");
        }
        return raw.substring(start, end + 1);
    }
}
package com.legitify.document_analysis_service.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.legitify.document_analysis_service.service.impl.DocumentAnalysisServiceImpl;

public class DocumentTextSanitizer {
    private static final Logger logger = Logger.getLogger(DocumentAnalysisServiceImpl.class.getName());

    public static void trimHeadersFooters(List<Page> pages) {
        Map<String, Integer> firstLineCounts = new HashMap<>();
        Map<String, Integer> lastLineCounts = new HashMap<>();

        for (Page p : pages) {
            String[] lines = p.text.split("\\R");
            if (lines.length > 0) {
                String first = lines[0].trim();
                if (!first.isEmpty())
                    firstLineCounts.merge(first, 1, Integer::sum);
                String last = lines[lines.length - 1].trim();
                if (!last.isEmpty())
                    lastLineCounts.merge(last, 1, Integer::sum);
            }
        }

        int threshold = Math.max(2, pages.size() / 2);
        Set<String> headerCandidates = firstLineCounts.entrySet().stream()
                .filter(e -> e.getValue() >= threshold)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        Set<String> footerCandidates = lastLineCounts.entrySet().stream()
                .filter(e -> e.getValue() >= threshold)
                .map(Map.Entry::getKey).collect(Collectors.toSet());

        if (!headerCandidates.isEmpty() || !footerCandidates.isEmpty()) {
            logger.info(() -> "Detected repeated headers: " + headerCandidates + " footers: " + footerCandidates);
        }

        for (Page p : pages) {
            List<String> lines = new ArrayList<>(Arrays.asList(p.text.split("\\R")));
            if (!lines.isEmpty()) {
                if (!headerCandidates.isEmpty()) {
                    if (headerCandidates.contains(lines.get(0).trim()))
                        lines.remove(0);
                }
                if (!footerCandidates.isEmpty() && !lines.isEmpty()) {
                    if (footerCandidates.contains(lines.get(lines.size() - 1).trim()))
                        lines.remove(lines.size() - 1);
                }
            }
            p.text = lines.stream().collect(Collectors.joining("\n")).trim();
        }
    }

    public static String normalizeWhitespace(String s) {
        if (s == null)
            return "";
        String normalized = s.replace("\r\n", "\n").replace("\r", "\n");
        normalized = normalized.replaceAll("[ \\t]+", " ");
        normalized = normalized.replaceAll("\\n{3,}", "\n\n");
        return normalized.trim();
    }
}

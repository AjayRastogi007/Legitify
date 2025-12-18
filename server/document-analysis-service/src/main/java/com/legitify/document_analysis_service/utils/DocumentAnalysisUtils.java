package com.legitify.document_analysis_service.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DocumentAnalysisUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    private DocumentAnalysisUtils() {}

    public static String mergeResponses(
            List<JsonNode> results,
            Map<String, String> metadata,
            List<String> warnings,
            boolean ocrSuggested) {

        ObjectNode merged = mapper.createObjectNode();

        merged.put("schemaVersion", "1.2");
        merged.put("analysisMode", "conservative");
        merged.put("incomplete", false);

        double totalConfidence = 0.0;
        int confidenceCount = 0;

        Map<String, ObjectNode> mergedClauses = new LinkedHashMap<>();
        Map<String, ObjectNode> mergedRisks = new LinkedHashMap<>();

        for (JsonNode r : results) {
            if (r == null || !r.isObject()) continue;

            if (r.path("incomplete").asBoolean(false)) {
                merged.put("incomplete", true);
            }

            if (r.has("confidence")) {
                totalConfidence += r.path("confidence").asDouble(0.0);
                confidenceCount++;
            }

            mergeClauses(r, mergedClauses);
            mergeRisks(r, mergedRisks);
        }

        merged.set("clauses", toArray(mergedClauses));
        merged.set("risksSummary", toArray(mergedRisks));

        merged.put("confidence",
                confidenceCount > 0 ? totalConfidence / confidenceCount : 0.75);

        merged.set("metadata", mapper.valueToTree(metadata));
        merged.set("warnings", mapper.valueToTree(warnings));
        merged.put("ocrSuggested", ocrSuggested);

        merged.put("summary",
                "Merged and deduplicated " + mergedClauses.size() +
                        " clauses across " + results.size() + " analyzed sections.");

        return toJson(merged);
    }

    private static void mergeClauses(
            JsonNode r,
            Map<String, ObjectNode> mergedClauses) {

        if (!r.has("clauses")) return;

        for (JsonNode clause : r.get("clauses")) {
            String type = clause.path("type").asText("unknown");
            String snippet = normalize(clause.path("textSnippet").asText(""));
            String key = type + "|" + snippet;

            mergedClauses.merge(
                    key,
                    clause.deepCopy(),
                    DocumentAnalysisUtils::mergeClauseNodes
            );
        }
    }

    private static ObjectNode mergeClauseNodes(
            ObjectNode existing,
            ObjectNode incoming) {

        String mergedSeverity = mergeSeverity(
                existing.path("severity").asText("unknown"),
                incoming.path("severity").asText("unknown"));

        existing.put("severity", mergedSeverity);

        appendIfMissing(existing, "analysis", incoming);
        appendIfMissing(existing, "recommendedAction", incoming);

        return existing;
    }

    private static void mergeRisks(
            JsonNode r,
            Map<String, ObjectNode> mergedRisks) {

        if (!r.has("risksSummary")) return;

        for (JsonNode risk : r.get("risksSummary")) {
            String text = normalize(risk.path("risk").asText(""));
            String key = text.length() > 120 ? text.substring(0, 120) : text;

            mergedRisks.merge(
                    key,
                    risk.deepCopy(),
                    (a, b) -> {
                        double avg =
                                (a.path("confidence").asDouble(0.0)
                                        + b.path("confidence").asDouble(0.0)) / 2;
                        a.put("confidence", avg);
                        return a;
                    });
        }
    }

    private static ArrayNode toArray(Map<String, ObjectNode> map) {
        ArrayNode arr = mapper.createArrayNode();
        map.values().forEach(arr::add);
        return arr;
    }

    private static void appendIfMissing(
            ObjectNode target,
            String field,
            ObjectNode source) {

        String t = target.path(field).asText("");
        String s = source.path(field).asText("");

        if (!s.isBlank() && !t.contains(s)) {
            target.put(field, (t + " " + s).trim());
        }
    }

    private static String normalize(String text) {
        return text == null ? "" :
                text.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static String mergeSeverity(String s1, String s2) {
        List<String> order = List.of("low", "medium", "high", "critical");
        int i1 = order.indexOf(s1.toLowerCase());
        int i2 = order.indexOf(s2.toLowerCase());
        if (i1 < 0) return s2;
        if (i2 < 0) return s1;
        return order.get(Math.max(i1, i2));
    }

    private static String toJson(ObjectNode node) {
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            return "{\"error\":\"serialization_failed\"}";
        }
    }
}


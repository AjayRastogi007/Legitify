package com.legitify.document_analysis_service.service.impl;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.legitify.document_analysis_service.service.DocumentAnalysisService;
import com.legitify.document_analysis_service.utils.ExtractionResult;
import com.legitify.document_analysis_service.utils.Page;
import com.legitify.document_analysis_service.utils.TextExtractor;

@Service
public class DocumentAnalysisServiceImpl implements DocumentAnalysisService {

    private static final Logger logger = Logger.getLogger(DocumentAnalysisServiceImpl.class.getName());
    private static final Charset DEFAULT_TEXT_CHARSET = StandardCharsets.UTF_8;

    @Override
    public ExtractionResult getTextFromFile(MultipartFile file) {
        ExtractionResult result = extractStructuredText(file, false);
        if (!result.errors.isEmpty()) {
            logger.warning(() -> "Extraction errors: " + String.join("; ", result.errors));
        }
        return result;
    }

    @Override
    public ExtractionResult getTextFromPath(Path path) {
        ExtractionResult result = new ExtractionResult();
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);

        try (InputStream in = Files.newInputStream(path)) {
            if (name.endsWith(".pdf")) {
                return TextExtractor.extractFromPdf(in, result, false);
            } else if (name.endsWith(".docx")) {
                String text = TextExtractor.extractTextFromDocx(in);
                result.setFullTextAndPages(text, Collections.singletonList(new Page(1, text)));
                return result;
            } else if (name.endsWith(".txt")) {
                String text = TextExtractor.extractTextFromTxt(in, DEFAULT_TEXT_CHARSET);
                result.setFullTextAndPages(text, Collections.singletonList(new Page(1, text)));
                return result;
            } else {
                result.errors.add("Unsupported file type: " + name);
                return result;
            }
        } catch (Exception e) {
            result.errors.add("Error reading file from path: " + e.getMessage());
            return result;
        }
    }

    public ExtractionResult extractStructuredText(MultipartFile file, boolean forceOcr) {
        ExtractionResult out = new ExtractionResult();
        if (file == null || file.isEmpty()) {
            out.errors.add("No file uploaded.");
            return out;
        }

        String contentType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);

        try (InputStream in = file.getInputStream()) {
            if (isTextPlain(contentType, filename)) {
                String txt = TextExtractor.extractTextFromTxt(in, DEFAULT_TEXT_CHARSET);
                out.setFullTextAndPages(txt, Collections.singletonList(new Page(1, txt)));
                return out;
            } else if (isPdf(contentType, filename)) {
                return TextExtractor.extractFromPdf(in, out, forceOcr);
            } else if (isDocx(contentType, filename)) {
                String text = TextExtractor.extractTextFromDocx(in);
                out.setFullTextAndPages(text, Collections.singletonList(new Page(1, text)));
                return out;
            } else {
                out.errors.add("Unsupported file type: " + contentType + " / " + filename);
                return out;
            }
        } catch (IOException e) {
            out.errors.add("IO error during extraction: " + e.getMessage());
            logger.severe(e.toString());
            return out;
        }
    }

    private boolean isTextPlain(String contentType, String filename) {
        return contentType.contains("text/plain") || filename.endsWith(".txt");
    }

    private boolean isPdf(String contentType, String filename) {
        return contentType.contains("pdf") || filename.endsWith(".pdf");
    }

    private boolean isDocx(String contentType, String filename) {
        return contentType.contains("wordprocessingml.document") || filename.endsWith(".docx");
    }


    @Override
    public String generatePdfFromString(String jsonResponse) {
        logger.info(() -> "generatePdfFromString called, length=" + (jsonResponse == null ? -1 : jsonResponse.length()));

        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new IllegalArgumentException("jsonResponse is null or blank");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid JSON received from analysis service: " + e.getMessage(), e);
        }

        Path pdfPath;
        try {
            pdfPath = Files.createTempFile("policy-analysis-", ".pdf");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temporary PDF file", e);
        }

        Document document = new Document(PageSize.A4);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(pdfPath.toFile());
            PdfWriter.getInstance(document, fos);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Unable to write PDF to temporary file", e);
        }

        try {
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font headingFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10);

            if (root.has("error")) {
                document.add(new Paragraph("Policy Analysis Report - Failed", titleFont));
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("Error: " + root.path("error").asText(), normalFont));
                if (root.has("details")) {
                    document.add(Chunk.NEWLINE);
                    document.add(new Paragraph("Details:", headingFont));
                    for (JsonNode d : root.path("details")) {
                        document.add(new Paragraph("- " + safeText(d.asText()), normalFont));
                    }
                }
                return pdfPath.toAbsolutePath().toString();
            }

            document.add(new Paragraph("Policy Analysis Report", titleFont));
            document.add(Chunk.NEWLINE);

            if (root.path("incomplete").asBoolean(false)) {
                document.add(new Paragraph("NOTE: Analysis marked as incomplete.", headingFont));
            }
            if (root.path("ocrSuggested").asBoolean(false)) {
                document.add(new Paragraph("NOTE: OCR was suggested for this document.", normalFont));
            }
            JsonNode warnings = root.path("warnings");
            if (warnings.isArray() && !warnings.isEmpty()) {
                document.add(new Paragraph("Warnings:", headingFont));
                for (JsonNode w : warnings) {
                    document.add(new Paragraph("- " + safeText(w.asText()), normalFont));
                }
                document.add(Chunk.NEWLINE);
            }

            JsonNode metadata = root.path("metadata");
            document.add(new Paragraph("Pages Extracted: " + metadata.path("pages_extracted").asText("N/A"), normalFont));
            document.add(new Paragraph("Confidence Score: " + root.path("confidence").asText("N/A"), normalFont));
            document.add(Chunk.NEWLINE);

            List<JsonNode> clauses = new ArrayList<>();
            root.path("clauses").forEach(clauses::add);

            Map<String, Integer> severityOrder = Map.of("critical", 1, "high", 2, "medium", 3, "low", 4);

            clauses.sort(Comparator.comparingInt(c -> severityOrder.getOrDefault(c.path("severity").asText().toLowerCase(), 99)));

            long criticalCount = clauses.stream().filter(c -> "critical".equalsIgnoreCase(c.path("severity").asText())).count();
            long highCount = clauses.stream().filter(c -> "high".equalsIgnoreCase(c.path("severity").asText())).count();
            long mediumCount = clauses.stream().filter(c -> "medium".equalsIgnoreCase(c.path("severity").asText())).count();
            long lowCount = clauses.stream().filter(c -> "low".equalsIgnoreCase(c.path("severity").asText())).count();

            document.add(new Paragraph("Severity Summary", headingFont));
            document.add(new Paragraph("Critical Risk Clauses : " + criticalCount, normalFont));
            document.add(new Paragraph("High Risk Clauses     : " + highCount, normalFont));
            document.add(new Paragraph("Medium Risk Clauses   : " + mediumCount, normalFont));
            document.add(new Paragraph("Low Risk Clauses      : " + lowCount, normalFont));
            document.add(Chunk.NEWLINE);

            addSeveritySection(document, "CRITICAL SEVERITY CLAUSES", clauses, "critical", headingFont);
            addSeveritySection(document, "HIGH SEVERITY CLAUSES", clauses, "high", headingFont);
            addSeveritySection(document, "MEDIUM SEVERITY CLAUSES", clauses, "medium", headingFont);
            addSeveritySection(document, "LOW SEVERITY CLAUSES", clauses, "low", headingFont);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Risk Summary", headingFont));
            document.add(Chunk.NEWLINE);

            for (JsonNode risk : root.path("risksSummary")) {
                document.add(new Paragraph("- " + safeText(risk.path("risk").asText()) + " (Confidence: " + risk.path("confidence").asText("N/A") + ")", normalFont));
            }

            return pdfPath.toAbsolutePath().toString();

        } catch (Throwable t) {
            logger.severe("PDF generation failed: " + t);
            throw new RuntimeException("PDF generation failed: " + t.getMessage(), t);
        } finally {
            try {
                document.close();
            } catch (Throwable ignored) {
            }
            try {
                fos.close();
            } catch (Throwable ignored) {
            }
        }
    }

    private static String safeText(String s) {
        if (s == null) return "";
        return s.length() > 2000 ? s.substring(0, 2000) + "..." : s;
    }

    private Font getSeverityFont(String severity) {
        if ("critical".equalsIgnoreCase(severity)) {
            return new Font(Font.HELVETICA, 10, Font.BOLD, new Color(139, 0, 0));
        }
        if ("high".equalsIgnoreCase(severity)) {
            return new Font(Font.HELVETICA, 10, Font.BOLD, Color.RED);
        }
        if ("medium".equalsIgnoreCase(severity)) {
            return new Font(Font.HELVETICA, 10, Font.BOLD, Color.ORANGE);
        }
        return new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GREEN);
    }

    private void addSeveritySection(Document document, String title, List<JsonNode> clauses, String severity, Font headingFont) throws DocumentException {

        document.add(new Paragraph(title, headingFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 4, 2, 4});

        table.addCell("Type");
        table.addCell("Snippet");
        table.addCell("Severity");
        table.addCell("Recommended Action");

        for (JsonNode clause : clauses) {
            if (!severity.equalsIgnoreCase(clause.path("severity").asText())) {
                continue;
            }

            table.addCell(safeText(clause.path("type").asText()));
            table.addCell(safeText(clause.path("textSnippet").asText()));

            Font severityFont = getSeverityFont(clause.path("severity").asText());
            table.addCell(new Paragraph(clause.path("severity").asText().toUpperCase(), severityFont));

            table.addCell(safeText(clause.path("recommendedAction").asText()));
        }

        document.add(table);
        document.add(Chunk.NEWLINE);
    }


}

package com.legitify.document_analysis_service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

public class TextExtractor {

    public static String extractTextFromTxt(InputStream in, Charset charset) throws IOException {
        try (Scanner s = new Scanner(in, charset)) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }

    public static String extractTextFromDocx(InputStream in) throws IOException {
        try (XWPFDocument docx = new XWPFDocument(in)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph para : docx.getParagraphs()) {
                sb.append(para.getText()).append("\n");
            }
            return sb.toString().trim();
        }
    }

    public static ExtractionResult extractFromPdf(InputStream in, ExtractionResult out, boolean forceOcr) throws IOException {
        Path tempPdf = Files.createTempFile("uploaded-", ".pdf");
        try {
            Files.copy(in, tempPdf, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            try (PDDocument pdf = Loader.loadPDF(tempPdf.toFile())) {
                if (pdf.isEncrypted()) {
                    out.errors.add("Cannot extract text: PDF is password-protected.");
                    return out;
                }

                PDFTextStripper stripper = new PDFTextStripper();
                int pages = pdf.getNumberOfPages();
                out.metadata.put("page_count", String.valueOf(pages));

                List<Page> pageList = new ArrayList<>(pages);
                StringBuilder full = new StringBuilder();

                for (int p = 1; p <= pages; p++) {
                    stripper.setStartPage(p);
                    stripper.setEndPage(p);
                    String pageText = stripper.getText(pdf);
                    pageText = DocumentTextSanitizer.normalizeWhitespace(pageText);
                    pageList.add(new Page(p, pageText));
                    full.append(pageText).append("\n<<PAGE_BREAK_").append(p).append(">>\n");
                }

                long totalTextLength = pageList.stream().mapToLong(pg -> pg.text.length()).sum();
                boolean likelyScanned = totalTextLength < Math.max(100, pages * 20);
                if (likelyScanned) {
                    out.ocrSuggested = true;
                    out.warnings.add("Extracted text is small relative to pages; PDF may be scanned. Consider OCR.");
                }

                if (forceOcr || out.ocrSuggested) {
                    try {
                        String tessdataPath = System.getenv("TESSDATA_PREFIX");
                        List<String> ocrPages = PdfOcrExtractor.ocrPdf(tempPdf, tessdataPath, "eng");

                        List<Page> ocrPageList = new ArrayList<>();
                        StringBuilder ocrFull = new StringBuilder();
                        for (int i = 0; i < ocrPages.size(); i++) {
                            String txt = DocumentTextSanitizer.normalizeWhitespace(ocrPages.get(i));
                            ocrPageList.add(new Page(i + 1, txt));
                            ocrFull.append(txt).append("\n<<PAGE_BREAK_").append(i + 1).append(">>\n");
                        }
                        DocumentTextSanitizer.trimHeadersFooters(ocrPageList);
                        out.setFullTextAndPages(ocrFull.toString().trim(), ocrPageList);
                        out.metadata.put("ocr_performed", "true");
                    } catch (Exception oex) {
                        out.warnings.add("OCR failed: " + oex.getMessage());
                        DocumentTextSanitizer.trimHeadersFooters(pageList);
                        out.setFullTextAndPages(full.toString().trim(), pageList);
                    }
                } else {
                    DocumentTextSanitizer.trimHeadersFooters(pageList);
                    out.setFullTextAndPages(full.toString().trim(), pageList);
                }

                return out;
            }
        } finally {
            try {
                Files.deleteIfExists(tempPdf);
            } catch (IOException e) {

            }
        }
    }
}

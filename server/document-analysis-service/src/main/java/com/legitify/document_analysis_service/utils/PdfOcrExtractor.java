package com.legitify.document_analysis_service.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

public class PdfOcrExtractor {
    private static final Logger logger = Logger.getLogger(PdfOcrExtractor.class.getName());

     public static List<String> ocrPdf(Path pdfPath, String tessdataPath, String lang) throws IOException {
        List<String> pages = new ArrayList<>();
        try(PDDocument pdf = Loader.loadPDF(pdfPath.toFile())) {
            PDFRenderer renderer = new PDFRenderer(pdf);
            ITesseract tesseract = new Tesseract();

            if(tessdataPath != null && !tessdataPath.isBlank()) {
                tesseract.setDatapath(tessdataPath);
            }
            if (lang != null && !lang.isBlank()) {
                tesseract.setLanguage(lang);
            } else {
                tesseract.setLanguage("eng");
            }

            final int dpi = 300;
            int pageCount = pdf.getNumberOfPages();
            for (int p = 0; p < pageCount; p++) {
                BufferedImage image = null;
                try {
                    image = renderer.renderImageWithDPI(p, dpi, ImageType.RGB);
                    String result = tesseract.doOCR(image);
                    pages.add(result == null ? "" : result.trim());
                } catch (TesseractException te) {
                    int finalP = p;
                    logger.warning(() -> "Tesseract failed on page " + (finalP + 1) + ": " + te.getMessage());
                    pages.add("");
                } catch (OutOfMemoryError oom) {
                    logger.severe("OutOfMemory while rendering PDF page for OCR. Consider lowering DPI or increasing heap.");
                    throw new IOException("OOM rendering PDF for OCR; try lower DPI or increase heap", oom);
                } finally {
                    image = null;
                }
            }
        }
        return pages;
     }
}
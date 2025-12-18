package com.legitify.document_analysis_service.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtractionResult {
        public String fullText = "";
        public final List<Page> pages = new ArrayList<>();
        public final Map<String, String> metadata = new HashMap<>();
        public final List<String> errors = new ArrayList<>();
        public final List<String> warnings = new ArrayList<>();
        public boolean ocrSuggested = false;

        public void setFullTextAndPages(String full, List<Page> pageList) {
            this.fullText = (full == null) ? "" : full;
            pages.clear();
            if (pageList != null)
                pages.addAll(pageList);
            metadata.put("pages_extracted", String.valueOf(pages.size()));
        }

        public String fullText() {
            if (fullText != null && !fullText.isEmpty())
                return fullText;
            return pages.stream().map(p -> p.text).collect(Collectors.joining("\n<<PAGE_BREAK>>\n"));
        }
    }

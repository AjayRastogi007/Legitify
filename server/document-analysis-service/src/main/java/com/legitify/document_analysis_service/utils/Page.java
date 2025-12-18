package com.legitify.document_analysis_service.utils;

public class Page {
        public final int pageNumber;
        public String text;

        public Page(int num, String text) {
            this.pageNumber = num;
            this.text = text == null ? "" : text;
        }
    }

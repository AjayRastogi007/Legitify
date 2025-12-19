package com.legitify.document_analysis_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DocumentAnalysisServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentAnalysisServiceApplication.class, args);
	}

}

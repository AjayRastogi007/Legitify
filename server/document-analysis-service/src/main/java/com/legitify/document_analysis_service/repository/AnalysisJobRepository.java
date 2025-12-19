package com.legitify.document_analysis_service.repository;

import com.legitify.document_analysis_service.entity.AnalysisJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisJobRepository extends JpaRepository<AnalysisJob, String> {
}

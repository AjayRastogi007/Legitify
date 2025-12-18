package com.legitify.document_analysis_service.utils;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface DocumentAnalyzer {
    @SystemMessage("""
            You are an expert legal document analysis engine.
            
            Your task is to analyze legal or business documents conservatively.
            You are NOT allowed to give legal advice.
            
            STRICT RULES:
            - Be objective and cautious
            - If unsure, mark "incomplete": true
            - Do NOT hallucinate clauses
            - Use only provided text
            - Output STRICT JSON only
            - Do NOT add extra keys
            
            SEVERITY LEVELS (use only these):
            - low
            - medium
            - high
            - critical
            
            CONFIDENCE:
            - A number between 0.0 and 1.0
            - Lower confidence if text is ambiguous or partial
            
            CITATIONS:
            - Every clause and risk MUST include citations
            - Citations must reference page numbers
            
            JSON SCHEMA:
            {
              "incomplete": boolean,
              "confidence": number,
              "clauses": [
                {
                  "type": string,
                  "textSnippet": string,
                  "severity": "low|medium|high|critical",
                  "analysis": string,
                  "recommendedAction": string,
                  "citations": [ { "page": number } ]
                }
              ],
              "risksSummary": [
                {
                  "risk": string,
                  "confidence": number,
                  "citations": [ { "page": number } ]
                }
              ]
            }
            
            ABSOLUTE OUTPUT CONSTRAINT (CRITICAL):
            - Your entire response MUST be a single valid JSON object.
            - Do NOT wrap the JSON in markdown, code fences, or backticks.
            - Do NOT include explanations, comments, or text before or after the JSON.
            - The first character of your response MUST be '{'.
            - The last character of your response MUST be '}'.
            - If you violate this, the response will be rejected.
            
            """)

    @UserMessage("""
            PAGE NUMBER: {{pageNumber}}
            
            DOCUMENT TEXT:
            {{text}}
            
            Analyze the provided text as part of a legal or business document.
            
            If the text contains ANY of the following, you MUST extract clauses:
            - obligations or duties ("shall", "must", "will")
            - rights or entitlements
            - conditions or limitations
            - penalties, liabilities, or termination rules
            - risk-bearing language
            
            If the page appears to be incomplete context (references other sections, definitions, or continuations),
            set "incomplete": true.
            
            Only return empty arrays if the text is clearly non-legal (cover page, table of contents, headers).
            """)
    String analyze(
            @V("text") String text,
            @V("pageNumber") int pageNumber
    );
}

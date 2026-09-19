package com.demo.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FraudAnalysisResult {
    private int riskScore;
    private String riskLevel;
    private List<String> reasons;
    private String recommendation;
    private double confidence;
}

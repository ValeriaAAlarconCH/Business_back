package com.diabetes.business_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionResponseDto {
    private String predictedClass;
    private String predictedClassEs;  // Añadido para soporte en español
    private Double probability;
    private Map<String, Double> probabilities;
    private Map<String, Double> featureImportance;
    private Boolean success;
    private String message;

    public String getPredictedClassEs() {
        return predictedClassEs != null ? predictedClassEs : predictedClass;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success != null ? success : false;
    }
}
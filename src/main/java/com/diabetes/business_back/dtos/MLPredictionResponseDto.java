package com.diabetes.business_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MLPredictionResponseDto implements Serializable {
    private String predictedClass;
    private Double probability;
    private Map<String, Double> probabilities;
    private Map<String, Double> featureImportance;
}

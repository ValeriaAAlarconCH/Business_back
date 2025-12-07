package com.diabetes.business_back.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("predictedClass")
    private String predictedClass;

    @JsonProperty("probability")
    private Double probability;

    @JsonProperty("probabilities")
    private Map<String, Double> probabilities;

    @JsonProperty("featureImportance")
    private Map<String, Double> featureImportance;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("message")
    private String message;

    // Añade getters y setters si no los tienes con Lombok
    public String getPredictedClassEs() {
        if (predictedClass == null) return "Desconocido";

        switch (predictedClass) {
            case "Type 1 Diabetes": return "Diabetes Tipo 1";
            case "Type 2 Diabetes": return "Diabetes Tipo 2";
            case "Prediabetic": return "Prediabetes";
            case "Gestational Diabetes": return "Diabetes Gestacional";
            case "LADA": return "Diabetes Autoimmune Latente en Adultos";
            case "MODY": return "MODY (Diabetes de la Madurez de Inicio Juvenil)";
            case "Steroid-Induced Diabetes": return "Diabetes Inducida por Esteroides";
            case "Secondary Diabetes": return "Diabetes Secundaria";
            case "Type 3c Diabetes (Pancreatogenic Diabetes)": return "Diabetes Tipo 3c (Pancreatogénica)";
            case "Cystic Fibrosis-Related Diabetes (CFRD)": return "Diabetes Relacionada con Fibrosis Quística";
            case "Wolcott-Rallison Syndrome": return "Síndrome de Wolcott-Rallison";
            case "Wolfram Syndrome": return "Síndrome de Wolfram";
            default: return predictedClass;
        }
    }
}
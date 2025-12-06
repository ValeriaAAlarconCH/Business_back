package com.diabetes.business_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    public Map<String, Double> getTopProbabilities(int topN) {
        if (probabilities == null || probabilities.isEmpty()) {
            return Map.of();
        }

        return probabilities.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }

    public static Map<String, String> getMapeoTiposDiabetes() {
        Map<String, String> mapeo = new HashMap<>();
        mapeo.put("Type 1 Diabetes", "Diabetes Tipo 1");
        mapeo.put("Type 2 Diabetes", "Diabetes Tipo 2");
        mapeo.put("Prediabetic", "Prediabetes");
        mapeo.put("Gestational Diabetes", "Diabetes Gestacional");
        mapeo.put("LADA", "Diabetes Autoimmune Latente en Adultos");
        mapeo.put("MODY", "MODY (Diabetes de la Madurez de Inicio Juvenil)");
        mapeo.put("Steroid-Induced Diabetes", "Diabetes Inducida por Esteroides");
        mapeo.put("Secondary Diabetes", "Diabetes Secundaria");
        mapeo.put("Type 3c Diabetes (Pancreatogenic Diabetes)", "Diabetes Tipo 3c (Pancreatogénica)");
        mapeo.put("Cystic Fibrosis-Related Diabetes (CFRD)", "Diabetes Relacionada con Fibrosis Quística");
        mapeo.put("Wolcott-Rallison Syndrome", "Síndrome de Wolcott-Rallison");
        mapeo.put("Wolfram Syndrome", "Síndrome de Wolfram");
        return mapeo;
    }

    public static List<String> getTodosTiposEnIngles() {
        return Arrays.asList(
                "Steroid-Induced Diabetes",
                "Prediabetic",
                "Type 1 Diabetes",
                "Wolfram Syndrome",
                "LADA",
                "Type 2 Diabetes",
                "Wolcott-Rallison Syndrome",
                "Secondary Diabetes",
                "Type 3c Diabetes (Pancreatogenic Diabetes)",
                "Gestational Diabetes",
                "Cystic Fibrosis-Related Diabetes (CFRD)",
                "MODY"
        );
    }

    public static List<String> getTodosTiposEnEspanol() {
        return Arrays.asList(
                "Diabetes Inducida por Esteroides",
                "Prediabetes",
                "Diabetes Tipo 1",
                "Síndrome de Wolfram",
                "Diabetes Autoimmune Latente en Adultos",
                "Diabetes Tipo 2",
                "Síndrome de Wolcott-Rallison",
                "Diabetes Secundaria",
                "Diabetes Tipo 3c (Pancreatogénica)",
                "Diabetes Gestacional",
                "Diabetes Relacionada con Fibrosis Quística",
                "MODY (Diabetes de la Madurez de Inicio Juvenil)"
        );
    }
}
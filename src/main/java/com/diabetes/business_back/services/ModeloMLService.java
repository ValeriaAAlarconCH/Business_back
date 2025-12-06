package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.MLPredictionRequestDto;
import com.diabetes.business_back.dtos.MLPredictionResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class ModeloMLService {

    private Object modeloML; // Aquí cargarás tu modelo pickle
    private Map<String, Integer> labelEncoder;
    private List<String> featureNames;
    private List<String> targetClasses;

    @PostConstruct
    public void init() {
        try {
            cargarModelo();
            inicializarConfiguraciones();
            log.info("Modelo ML cargado exitosamente");
        } catch (Exception e) {
            log.error("Error al cargar el modelo ML: {}", e.getMessage());
            // En desarrollo, puedes usar un modelo simulado
            inicializarModeloSimulado();
        }
    }

    private void cargarModelo() throws IOException, ClassNotFoundException {
        // Metodo 1: Si tienes el modelo en formato pickle de Python
        // Necesitarás una biblioteca como JPMML o crear una API Python

        // Metodo 2: Exportar a formato ONNX y usar biblioteca Java
        // Más recomendado para producción

        // Por ahora, vamos a crear un enfoque híbrido
        log.info("Cargando modelo ML...");

        // 1. Primero intenta cargar desde pickle usando Python bridge
        // 2. Si no funciona, usa modelo simulado para desarrollo

        // Para pickle necesitarías:
        // - py4j o jep para conectar con Python
        // - O exportar a PMML/ONNX

        // Por ahora, inicializamos como simulado
        inicializarModeloSimulado();
    }

    private void inicializarModeloSimulado() {
        this.labelEncoder = new HashMap<>();
        this.featureNames = Arrays.asList(
                "marcadores_geneticos", "autoanticuerpos", "antecedentes_familiares",
                "factores_ambientales", "etnicidad", "habitos_alimenticios",
                "prueba_tolerancia_glucosa", "pruebas_funcion_hepatica",
                "diagnostico_fibrosis_quistica", "uso_esteroides", "pruebas_geneticas",
                "historial_embarazos", "diabetes_gestacional_previa", "historial_pcos",
                "estado_tabaquismo", "sintomas_inicio_temprano", "factores_socioeconomicos",
                "evaluaciones_neurologicas", "consumo_alcohol", "actividad_fisica",
                "prueba_orina", "Target", "indice_masa_corporal", "circunferencia_cintura",
                "aumento_peso_embarazo", "niveles_insulina", "funcion_pulmonar", "edad",
                "presion_arterial", "salud_pancreatica", "niveles_enzimas_digestivas",
                "niveles_colesterol", "niveles_glucosa", "peso_nacimiento"
        );

        this.targetClasses = Arrays.asList(
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

        log.info("Modelo simulado inicializado con {} características y {} clases",
                featureNames.size(), targetClasses.size());
    }

    private void inicializarConfiguraciones() {
        // Configuración de mapeo de valores categóricos a numéricos
        labelEncoder = new HashMap<>();

        // Ejemplo: mapeo para variables categóricas
        Map<String, Map<String, Integer>> encodings = new HashMap<>();

        // Para marcadores_geneticos
        Map<String, Integer> marcadoresMap = new HashMap<>();
        marcadoresMap.put("Negative", 0);
        marcadoresMap.put("Positive", 1);
        encodings.put("marcadores_geneticos", marcadoresMap);

        // Para autoanticuerpos
        Map<String, Integer> autoMap = new HashMap<>();
        autoMap.put("Negative", 0);
        autoMap.put("Positive", 1);
        encodings.put("autoanticuerpos", autoMap);

        // Para antecedentes_familiares
        Map<String, Integer> familiaMap = new HashMap<>();
        familiaMap.put("No", 0);
        familiaMap.put("Yes", 1);
        encodings.put("antecedentes_familiares", familiaMap);

        // ... agregar todos los mapeos necesarios
    }

    public MLPredictionResponseDto predecir(MLPredictionRequestDto request) {
        try {
            Map<String, Object> features = request.getFeatures();

            // 1. Preparar características para el modelo
            double[] featureArray = prepararCaracteristicas(features);

            // 2. Hacer predicción (aquí integrarías tu modelo real)
            String predictedClass = hacerPrediccionReal(featureArray);
            Double probability = calcularProbabilidad(predictedClass, features);

            // 3. Obtener importancia de características (si el modelo lo soporta)
            Map<String, Double> featureImportance = calcularImportancia(features);

            // 4. Calcular probabilidades para todas las clases
            Map<String, Double> probabilities = calcularProbabilidadesPorClase(features);

            return new MLPredictionResponseDto(
                    predictedClass,
                    probability,
                    probabilities,
                    featureImportance
            );

        } catch (Exception e) {
            log.error("Error en predicción: {}", e.getMessage());
            return prediccionSimulada(request.getFeatures());
        }
    }

    private double[] prepararCaracteristicas(Map<String, Object> features) {
        // Convertir características a array numérico según cómo entrenaste el modelo
        double[] array = new double[featureNames.size()];

        for (int i = 0; i < featureNames.size(); i++) {
            String featureName = featureNames.get(i);
            Object value = features.get(featureName);

            if (value == null) {
                array[i] = 0.0; // o valor por defecto/imputación
            } else if (value instanceof Number) {
                array[i] = ((Number) value).doubleValue();
            } else if (value instanceof String) {
                // Convertir categórico a numérico
                array[i] = convertirCategoricoANumerico(featureName, (String) value);
            } else {
                array[i] = 0.0;
            }
        }

        return array;
    }

    private double convertirCategoricoANumerico(String featureName, String value) {
        // Mapeo básico - deberías usar el mismo encoding que en tu entrenamiento
        switch (featureName) {
            case "marcadores_geneticos":
            case "autoanticuerpos":
            case "pruebas_geneticas":
                return value.equals("Positive") ? 1.0 : 0.0;

            case "antecedentes_familiares":
            case "diabetes_gestacional_previa":
            case "historial_pcos":
            case "diagnostico_fibrosis_quistica":
            case "uso_esteroides":
            case "sintomas_inicio_temprano":
                return value.equals("Yes") ? 1.0 : 0.0;

            case "factores_ambientales":
                return value.equals("Present") ? 1.0 : 0.0;

            case "etnicidad":
                return value.equals("High Risk") ? 1.0 : 0.0;

            case "actividad_fisica":
                switch (value) {
                    case "Low": return 0.0;
                    case "Moderate": return 1.0;
                    case "High": return 2.0;
                    default: return 1.0;
                }

            case "habitos_alimenticios":
                return value.equals("Healthy") ? 1.0 : 0.0;

            case "factores_socioeconomicos":
                switch (value) {
                    case "Low": return 0.0;
                    case "Medium": return 1.0;
                    case "High": return 2.0;
                    default: return 1.0;
                }

            case "estado_tabaquismo":
                return value.equals("Smoker") ? 1.0 : 0.0;

            case "consumo_alcohol":
                switch (value) {
                    case "Low": return 0.0;
                    case "Moderate": return 1.0;
                    case "High": return 2.0;
                    default: return 1.0;
                }

            case "prueba_tolerancia_glucosa":
            case "pruebas_funcion_hepatica":
                return value.equals("Abnormal") ? 1.0 : 0.0;

            case "historial_embarazos":
                return value.equals("Complications") ? 1.0 : 0.0;

            case "prueba_orina":
                if (value.equals("Normal")) return 0.0;
                else if (value.contains("Glucose")) return 1.0;
                else if (value.contains("Ketones")) return 2.0;
                else return 3.0; // Protein Present

            default:
                return 0.0;
        }
    }

    private String hacerPrediccionReal(double[] features) {
        // AQUÍ VA TU LÓGICA REAL DE PREDICCIÓN

        // Opción 1: Si exportaste a ONNX
        // return predecirConONNX(features);

        // Opción 2: Si creaste API Python
        // return llamarAPIPython(features);

        // Por ahora, simulación basada en reglas simples
        return simularPrediccionConReglas(features);
    }

    private String simularPrediccionConReglas(double[] features) {
        // Esta es solo una simulación - reemplázala con tu modelo real

        // Índices aproximados basados en tu lista de features
        int idxGlucosa = 32; // niveles_glucosa
        int idxInsulina = 25; // niveles_insulina
        int idxEdad = 27; // edad
        int idxAutoanticuerpos = 1; // autoanticuerpos
        int idxEmbarazo = 11; // historial_embarazos

        double glucosa = features[idxGlucosa];
        double insulina = features[idxInsulina];
        double edad = features[idxEdad];
        double autoanticuerpos = features[idxAutoanticuerpos];
        double embarazo = features[idxEmbarazo];

        // Reglas simples de ejemplo
        if (edad < 1 && glucosa > 200) {
            return "Wolcott-Rallison Syndrome";
        } else if (autoanticuerpos == 1.0 && edad < 30) {
            return "Type 1 Diabetes";
        } else if (embarazo == 1.0 && glucosa > 140) {
            return "Gestational Diabetes";
        } else if (glucosa > 125 && insulina > 25) {
            return "Type 2 Diabetes";
        } else if (glucosa >= 100 && glucosa <= 125) {
            return "Prediabetic";
        } else {
            return "Type 2 Diabetes"; // Default
        }
    }

    private Double calcularProbabilidad(String predictedClass, Map<String, Object> features) {
        // Simulación - en tu modelo real obtendrías esto del predict_proba
        Random rand = new Random();
        return 0.7 + (rand.nextDouble() * 0.25); // Entre 0.7 y 0.95
    }

    private Map<String, Double> calcularProbabilidadesPorClase(Map<String, Object> features) {
        Map<String, Double> probabilities = new HashMap<>();
        Random rand = new Random();

        for (String className : targetClasses) {
            probabilities.put(className, rand.nextDouble());
        }

        // Normalizar para que sumen 1
        double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        probabilities.replaceAll((k, v) -> v / total);

        return probabilities;
    }

    private Map<String, Double> calcularImportancia(Map<String, Object> features) {
        Map<String, Double> importance = new HashMap<>();

        // Asignar importancia basada en reglas simples
        importance.put("niveles_glucosa", 0.85);
        importance.put("niveles_insulina", 0.75);
        importance.put("edad", 0.65);
        importance.put("indice_masa_corporal", 0.60);
        importance.put("autoanticuerpos", 0.55);

        return importance;
    }

    private MLPredictionResponseDto prediccionSimulada(Map<String, Object> features) {
        // Predicción de emergencia cuando el modelo no está disponible
        String predictedClass = "Type 2 Diabetes";
        Double probability = 0.8;

        Map<String, Double> probabilities = new HashMap<>();
        probabilities.put("Type 2 Diabetes", 0.8);
        probabilities.put("Type 1 Diabetes", 0.1);
        probabilities.put("Prediabetic", 0.05);
        probabilities.put("Gestational Diabetes", 0.05);

        Map<String, Double> featureImportance = new HashMap<>();
        featureImportance.put("niveles_glucosa", 0.9);
        featureImportance.put("edad", 0.7);

        return new MLPredictionResponseDto(
                predictedClass,
                probability,
                probabilities,
                featureImportance
        );
    }
}

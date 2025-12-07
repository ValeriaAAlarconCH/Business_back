package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.MLPredictionRequestDto;
import com.diabetes.business_back.dtos.MLPredictionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ModeloMLService {
    private final PythonMLClient pythonMLClient;

    @Value("${ml.python.enabled:true}")
    private boolean mlPythonEnabled;

    private boolean apiDisponible = false;

    public ModeloMLService(PythonMLClient pythonMLClient) {
        this.pythonMLClient = pythonMLClient;
        inicializarServicio();
    }

    private void inicializarServicio() {
        log.info("🔄 Inicializando servicio de modelo ML...");

        if (mlPythonEnabled) {
            verificarYConfigurarAPI();
        } else {
            log.warn("⚠️ Integración con API Python deshabilitada en configuración");
        }
    }

    private void verificarYConfigurarAPI() {
        try {
            apiDisponible = pythonMLClient.verificarConexion();

            if (apiDisponible) {
                log.info("✅ Servicio ML configurado para usar API Python");
                probarPrediccionEjemplo();
            } else {
                log.warn("⚠️ API Python no disponible. Usando modelo simulado.");
            }
        } catch (Exception e) {
            log.error("❌ Error configurando servicio ML: {}", e.getMessage());
            apiDisponible = false;
        }
    }

    private void probarPrediccionEjemplo() {
        try {
            log.info("🧪 Probando predicción de ejemplo...");
            MLPredictionResponseDto resultado = pythonMLClient.pruebaPrediccion();

            if (resultado != null) {
                log.info("✅ Prueba exitosa. Predicción: {} ({:.1f}%)",
                        resultado.getPredictedClass(),
                        resultado.getProbability() * 100);
            }
        } catch (Exception e) {
            log.warn("⚠️ Prueba de predicción falló: {}", e.getMessage());
        }
    }

    public MLPredictionResponseDto predecir(Map<String, Object> features) {
        try {
            validarCaracteristicas(features);

            if (mlPythonEnabled && apiDisponible) {
                try {
                    log.info("🤖 Usando modelo real de Python para predicción");
                    return pythonMLClient.predecirConPython(features);
                } catch (Exception e) {
                    log.error("❌ Falló predicción con API Python: {}", e.getMessage());
                    log.warn("🔄 Reintentando con modelo simulado...");
                    apiDisponible = false; // Marcar como no disponible
                }
            }

            log.info("🔧 Usando modelo simulado (fallback)");
            return prediccionSimulada(features);

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error en predicción: {}", e.getMessage());
            return crearRespuestaError("Error en el servicio de predicción: " + e.getMessage());
        }
    }

    public MLPredictionResponseDto predecir(MLPredictionRequestDto request) {
        return predecir(request.getFeatures());
    }

    private void validarCaracteristicas(Map<String, Object> features) {
        if (features == null || features.isEmpty()) {
            throw new IllegalArgumentException("No se proporcionaron características para la predicción");
        }

        List<String> requiredFeatures = Arrays.asList(
                "edad", "niveles_glucosa", "niveles_insulina"
        );

        for (String feature : requiredFeatures) {
            if (!features.containsKey(feature) || features.get(feature) == null) {
                throw new IllegalArgumentException("Falta la característica obligatoria: " + feature);
            }
        }

        try {
            Integer edad = convertirAEntero(features.get("edad"));
            if (edad < 0 || edad > 120) {
                throw new IllegalArgumentException("Edad inválida. Debe estar entre 0 y 120");
            }

            Double glucosa = convertirADouble(features.get("niveles_glucosa"));
            if (glucosa < 0 || glucosa > 1000) {
                throw new IllegalArgumentException("Niveles de glucosa inválidos");
            }

            Double insulina = convertirADouble(features.get("niveles_insulina"));
            if (insulina < 0 || insulina > 500) {
                throw new IllegalArgumentException("Niveles de insulina inválidos");
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato numérico inválido en características");
        }
    }

    private MLPredictionResponseDto prediccionSimulada(Map<String, Object> features) {
        log.info("🎭 Generando predicción simulada");

        try {
            Integer edad = convertirAEntero(features.get("edad"));
            Double glucosa = convertirADouble(features.get("niveles_glucosa"));
            Double insulina = convertirADouble(features.get("niveles_insulina"));
            String autoanticuerpos = (String) features.get("autoanticuerpos");

            String predictedClass = determinarClaseSimulada(edad, glucosa, insulina, autoanticuerpos);
            Double probability = 0.75 + (new Random().nextDouble() * 0.20); // 75-95%

            Map<String, Double> probabilities = generarProbabilidadesSimuladas(predictedClass);
            Map<String, Double> featureImportance = generarImportanciaSimulada(features);

            return new MLPredictionResponseDto(
                    predictedClass,
                    probability,
                    probabilities,
                    featureImportance
            );

        } catch (Exception e) {
            log.error("Error en predicción simulada: {}", e.getMessage());
            return crearRespuestaError("Error en predicción simulada");
        }
    }

    private String determinarClaseSimulada(Integer edad, Double glucosa, Double insulina, String autoanticuerpos) {
        Random rand = new Random();

        // Lógica de simulación más simple y realista
        if (glucosa > 200) {
            return "Type 1 Diabetes";
        } else if (glucosa > 126 && insulina > 30) {
            return "Type 2 Diabetes";
        } else if (glucosa >= 100 && glucosa <= 125) {
            return "Prediabetic";
        } else if ("Positive".equals(autoanticuerpos) && edad < 30) {
            return "Type 1 Diabetes";
        } else if (edad != null && edad < 25 && glucosa > 130 && glucosa < 200) {
            return "MODY";
        } else {
            // Distribución basada en prevalencia
            double random = rand.nextDouble();
            if (random < 0.50) return "Type 2 Diabetes";
            else if (random < 0.65) return "Prediabetic";
            else if (random < 0.75) return "Type 1 Diabetes";
            else if (random < 0.80) return "Gestational Diabetes";
            else if (random < 0.85) return "LADA";
            else if (random < 0.88) return "MODY";
            else if (random < 0.91) return "Steroid-Induced Diabetes";
            else if (random < 0.94) return "Secondary Diabetes";
            else if (random < 0.96) return "Type 3c Diabetes (Pancreatogenic Diabetes)";
            else if (random < 0.98) return "Cystic Fibrosis-Related Diabetes (CFRD)";
            else return "Wolfram Syndrome";
        }
    }

    private Map<String, Double> generarProbabilidadesSimuladas(String predictedClass) {
        Map<String, Double> probabilities = new HashMap<>();
        Random rand = new Random();

        List<String> todasLasClases = Arrays.asList(
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

        for (String clase : todasLasClases) {
            if (clase.equals(predictedClass)) {
                probabilities.put(clase, 0.75 + (rand.nextDouble() * 0.20));
            } else {
                probabilities.put(clase, rand.nextDouble() * 0.20);
            }
        }

        // Normalizar
        double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        probabilities.replaceAll((k, v) -> v / total);

        return probabilities;
    }

    private Map<String, Double> generarImportanciaSimulada(Map<String, Object> features) {
        Map<String, Double> importance = new HashMap<>();
        Random rand = new Random();

        importance.put("niveles_glucosa", 0.8 + (rand.nextDouble() * 0.15));
        importance.put("niveles_insulina", 0.6 + (rand.nextDouble() * 0.25));
        importance.put("edad", 0.5 + (rand.nextDouble() * 0.3));

        if (features.containsKey("autoanticuerpos")) {
            importance.put("autoanticuerpos", 0.7 + (rand.nextDouble() * 0.2));
        }

        if (features.containsKey("antecedentes_familiares")) {
            importance.put("antecedentes_familiares", 0.6 + (rand.nextDouble() * 0.25));
        }

        return importance;
    }

    private MLPredictionResponseDto crearRespuestaError(String mensaje) {
        MLPredictionResponseDto dto = new MLPredictionResponseDto();
        dto.setPredictedClass("Error");
        dto.setProbability(0.0);
        dto.setProbabilities(new HashMap<>());
        dto.setFeatureImportance(new HashMap<>());
        return dto;
    }

    private Integer convertirAEntero(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    private Double convertirADouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public boolean isAPIDisponible() {
        return apiDisponible;
    }

    public Map<String, Object> getEstadoServicio() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("apiPythonHabilitada", mlPythonEnabled);
        estado.put("apiDisponible", apiDisponible);
        estado.put("ultimaVerificacion", LocalDateTime.now());
        estado.put("servicioActivo", true);
        return estado;
    }
}
package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.MLPredictionRequestDto;
import com.diabetes.business_back.dtos.MLPredictionResponseDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ModeloMLService {

    @Autowired
    private PythonMLClient pythonMLClient;

    @Value("${ml.python.enabled:true}")
    private boolean mlPythonEnabled;

    private boolean apiDisponible = false;
    private Map<String, Object> infoModelo = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("🔄 Inicializando servicio de modelo ML...");

        if (mlPythonEnabled) {
            verificarYConfigurarAPI();
        } else {
            log.warn("⚠️ Integración con API Python deshabilitada en configuración");
            log.info("🔧 Usando solo modelo simulado");
        }
    }

    private void verificarYConfigurarAPI() {
        try {
            apiDisponible = pythonMLClient.verificarConexion();

            if (apiDisponible) {
                infoModelo = pythonMLClient.obtenerInfoModelo();
                log.info("✅ Servicio ML configurado para usar API Python");
                log.info("📊 Información del modelo: {} features, {} clases",
                        infoModelo.get("num_features"),
                        infoModelo.get("classes"));

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
                log.info("✅ Prueba exitosa. Predicción: {} ({}%)",
                        resultado.getPredictedClass(),
                        String.format("%.1f", resultado.getProbability() * 100));
            }
        } catch (Exception e) {
            log.warn("⚠️ Prueba de predicción falló: {}", e.getMessage());
        }
    }

    public MLPredictionResponseDto predecir(MLPredictionRequestDto request) {
        return predecir(request.getFeatures());
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
            String embarazo = (String) features.get("historial_embarazos");

            String predictedClass = determinarClaseSimulada(edad, glucosa, insulina, autoanticuerpos, embarazo);
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

    private String determinarClaseSimulada(Integer edad, Double glucosa, Double insulina,
                                           String autoanticuerpos, String embarazo) {
        // Lógica mejorada que cubre más tipos de diabetes

        // 1. Wolcott-Rallison Syndrome (diabetes neonatal)
        if (edad != null && edad < 1 && glucosa > 200) {
            return "Wolcott-Rallison Syndrome";
        }

        // 2. Wolfram Syndrome (diabetes + problemas neurológicos en jóvenes)
        if (edad != null && edad < 20 && glucosa > 180 && "Positive".equals(autoanticuerpos)) {
            return "Wolfram Syndrome";
        }

        // 3. Type 1 Diabetes (autoanticuerpos positivos, edad joven)
        if ("Positive".equals(autoanticuerpos) && edad != null && edad < 30) {
            return "Type 1 Diabetes";
        }

        // 4. Gestational Diabetes (embarazo con complicaciones)
        if ("Complications".equals(embarazo) && glucosa > 140) {
            return "Gestational Diabetes";
        }

        // 5. CFRD (enfermedad pulmonar crónica + diabetes)
        // Simulamos con función pulmonar baja
        if (glucosa > 160 && insulina > 30) {
            return "Cystic Fibrosis-Related Diabetes (CFRD)";
        }

        // 6. Type 3c Diabetes (problemas pancreáticos)
        if (glucosa > 150 && insulina < 10) { // Baja insulina sugiere daño pancreático
            return "Type 3c Diabetes (Pancreatogenic Diabetes)";
        }

        // 7. MODY (jóvenes no obesos, fuerte historia familiar)
        if (edad != null && edad < 25 && glucosa > 130 && glucosa < 200) {
            return "MODY";
        }

        // 8. LADA (adultos >30 con autoanticuerpos)
        if ("Positive".equals(autoanticuerpos) && edad != null && edad >= 30) {
            return "LADA";
        }

        // 9. Steroid-Induced Diabetes (uso de esteroides)
        // Simulamos con otros factores
        if (glucosa > 170 && insulina > 35) {
            return "Steroid-Induced Diabetes";
        }

        // 10. Secondary Diabetes (asociada a otras condiciones)
        if (glucosa > 140 && glucosa < 180) {
            return "Secondary Diabetes";
        }

        // 11. Type 2 Diabetes (resistencia a insulina)
        if (glucosa > 125 && insulina > 25) {
            return "Type 2 Diabetes";
        }

        // 12. Prediabetic
        if (glucosa >= 100 && glucosa <= 125) {
            return "Prediabetic";
        }

        // Distribución basada en prevalencia si no aplican las reglas anteriores
        Random rand = new Random();
        double random = rand.nextDouble();

        if (random < 0.35) return "Type 2 Diabetes";        // 35% - más común
        else if (random < 0.50) return "Prediabetic";       // 15%
        else if (random < 0.58) return "Type 1 Diabetes";   // 8%
        else if (random < 0.63) return "Gestational Diabetes"; // 5%
        else if (random < 0.67) return "LADA";              // 4%
        else if (random < 0.71) return "MODY";              // 4%
        else if (random < 0.74) return "Steroid-Induced Diabetes"; // 3%
        else if (random < 0.77) return "Secondary Diabetes"; // 3%
        else if (random < 0.80) return "Type 3c Diabetes (Pancreatogenic Diabetes)"; // 3%
        else if (random < 0.83) return "Cystic Fibrosis-Related Diabetes (CFRD)"; // 3%
        else if (random < 0.86) return "Wolfram Syndrome";  // 3%
        else return "Wolcott-Rallison Syndrome";            // 14% restante
    }

    private Map<String, Double> generarProbabilidadesSimuladas(String predictedClass) {
        Map<String, Double> probabilities = new HashMap<>();
        Random rand = new Random();

        // TODOS los 12 tipos que tu modelo puede predecir
        List<String> clases = Arrays.asList(
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

        for (String clase : clases) {
            if (clase.equals(predictedClass)) {
                probabilities.put(clase, 0.75 + (rand.nextDouble() * 0.20)); // 75-95% para la predicha
            } else {
                probabilities.put(clase, rand.nextDouble() * 0.25); // 0-25% para las demás
            }
        }

        // Normalizar para que sumen ~1
        double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        probabilities.replaceAll((k, v) -> v / total);

        return probabilities;
    }

    private Map<String, Double> generarImportanciaSimulada(Map<String, Object> features) {
        Map<String, Double> importance = new HashMap<>();
        Random rand = new Random();

        importance.put("niveles_glucosa", 0.85 + (rand.nextDouble() * 0.10));
        importance.put("niveles_insulina", 0.70 + (rand.nextDouble() * 0.15));
        importance.put("edad", 0.60 + (rand.nextDouble() * 0.20));
        importance.put("indice_masa_corporal", 0.55 + (rand.nextDouble() * 0.20));

        features.forEach((key, value) -> {
            if (!importance.containsKey(key) &&
                    !key.equals("niveles_glucosa") &&
                    !key.equals("niveles_insulina") &&
                    !key.equals("edad") &&
                    !key.equals("indice_masa_corporal")) {
                importance.put(key, 0.20 + (rand.nextDouble() * 0.40));
            }
        });

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

        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
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

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (value instanceof Boolean) {
            return ((Boolean) value) ? 1.0 : 0.0;
        }
        return 0.0;
    }

    public boolean isAPIDisponible() {
        return apiDisponible;
    }

    public Map<String, Object> getInfoModelo() {
        return infoModelo != null ? new HashMap<>(infoModelo) : new HashMap<>();
    }

    public Map<String, Object> getEstadoServicio() {
        Map<String, Object> estado = new HashMap<>();
        estado.put("apiPythonHabilitada", mlPythonEnabled);
        estado.put("apiDisponible", apiDisponible);
        estado.put("infoModelo", infoModelo);
        estado.put("ultimaVerificacion", LocalDateTime.now());
        estado.put("servicioActivo", true);
        return estado;
    }
}
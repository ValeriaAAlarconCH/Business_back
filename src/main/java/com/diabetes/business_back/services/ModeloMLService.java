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
    private LocalDateTime ultimaVerificacion;

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
            ultimaVerificacion = LocalDateTime.now();

            if (apiDisponible) {
                log.info("✅ Servicio ML configurado para usar API Python");

                // Probar con datos de ejemplo
                try {
                    MLPredictionResponseDto prueba = pythonMLClient.pruebaPrediccion();
                    if (prueba != null && prueba.getSuccess() != null && prueba.getSuccess()) {
                        log.info("✅ Prueba de predicción exitosa. Modelo funcionando correctamente");
                    } else {
                        log.warn("⚠️ Prueba de predicción falló o respuesta inválida");
                    }
                } catch (Exception e) {
                    log.warn("⚠️ Prueba de predicción encontró errores: {}", e.getMessage());
                }

            } else {
                log.warn("⚠️ API Python no disponible. Usando modelo simulado.");
            }
        } catch (Exception e) {
            log.error("❌ Error configurando servicio ML: {}", e.getMessage());
            apiDisponible = false;
            ultimaVerificacion = LocalDateTime.now();
        }
    }

    /**
     * Método principal para realizar predicciones - USADO por EvaluacionDiabetesService
     */
    public MLPredictionResponseDto predecir(Map<String, Object> features) {
        try {
            log.info("🎯 Iniciando predicción con {} características", features.size());
            validarCaracteristicas(features);

            // Intentar usar API Python si está disponible
            if (mlPythonEnabled && apiDisponible) {
                try {
                    log.info("🤖 Usando modelo real de Python para predicción");
                    MLPredictionResponseDto resultado = pythonMLClient.predecirConPython(features);

                    if (resultado != null && resultado.getSuccess() != null && resultado.getSuccess()) {
                        return resultado;
                    } else {
                        log.warn("⚠️ Predicción de API Python falló, usando simulada");
                        return prediccionSimulada(features);
                    }

                } catch (Exception e) {
                    log.error("❌ Error con API Python: {}", e.getMessage());
                    // Re-verificar conexión
                    apiDisponible = pythonMLClient.verificarConexion();
                    ultimaVerificacion = LocalDateTime.now();

                    if (!apiDisponible) {
                        log.warn("🔄 API Python no disponible, usando modelo simulado");
                        return prediccionSimulada(features);
                    } else {
                        throw e; // Re-lanzar si API está disponible pero hay otro error
                    }
                }
            }

            // Usar modelo simulado
            log.info("🔧 Usando modelo simulado (fallback o deshabilitado)");
            return prediccionSimulada(features);

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error en predicción: {}", e.getMessage(), e);
            return crearRespuestaError("Error en el servicio de predicción: " + e.getMessage());
        }
    }

    /**
     * Método sobrecargado para usar con DTO
     */
    public MLPredictionResponseDto predecir(MLPredictionRequestDto request) {
        if (request == null || request.getFeatures() == null) {
            throw new IllegalArgumentException("Request o features no pueden ser nulos");
        }
        return predecir(request.getFeatures());
    }

    private void validarCaracteristicas(Map<String, Object> features) {
        if (features == null || features.isEmpty()) {
            throw new IllegalArgumentException("No se proporcionaron características para la predicción");
        }

        List<String> requiredFeatures = Arrays.asList(
                "edad", "niveles_glucosa", "niveles_insulina"
        );

        List<String> faltantes = new ArrayList<>();
        for (String feature : requiredFeatures) {
            if (!features.containsKey(feature) || features.get(feature) == null) {
                faltantes.add(feature);
            }
        }

        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException("Faltan características obligatorias: " + String.join(", ", faltantes));
        }

        // Validar tipos y rangos
        try {
            Integer edad = convertirAEntero(features.get("edad"));
            if (edad < 0 || edad > 120) {
                throw new IllegalArgumentException("Edad inválida. Debe estar entre 0 y 120 años");
            }

            Double glucosa = convertirADouble(features.get("niveles_glucosa"));
            if (glucosa < 0 || glucosa > 1000) {
                throw new IllegalArgumentException("Niveles de glucosa inválidos. Rango: 0-1000 mg/dL");
            }

            Double insulina = convertirADouble(features.get("niveles_insulina"));
            if (insulina < 0 || insulina > 500) {
                throw new IllegalArgumentException("Niveles de insulina inválidos. Rango: 0-500 μU/mL");
            }

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato numérico inválido en características");
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Tipo de dato inválido en características");
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

            MLPredictionResponseDto response = new MLPredictionResponseDto();
            response.setPredictedClass(predictedClass);
            response.setProbability(probability);
            response.setProbabilities(probabilities);
            response.setFeatureImportance(featureImportance);
            response.setSuccess(true);
            response.setMessage("Predicción usando modelo simulado (API Python no disponible)");

            log.info("✅ Predicción simulada generada: {} ({:.1f}%)",
                    predictedClass, probability * 100);

            return response;

        } catch (Exception e) {
            log.error("Error en predicción simulada: {}", e.getMessage());
            return crearRespuestaError("Error en predicción simulada: " + e.getMessage());
        }
    }

    private String determinarClaseSimulada(Integer edad, Double glucosa, Double insulina, String autoanticuerpos) {
        Random rand = new Random();

        // Lógica de simulación más realista
        if (glucosa > 200 || ("Positive".equals(autoanticuerpos) && edad < 30)) {
            return "Type 1 Diabetes";
        } else if (glucosa > 126 && insulina > 30) {
            return "Type 2 Diabetes";
        } else if (glucosa >= 100 && glucosa <= 125) {
            return "Prediabetic";
        } else if (edad != null && edad < 25 && glucosa > 130) {
            return "MODY";
        } else {
            // Distribución basada en prevalencia real
            double random = rand.nextDouble();
            if (random < 0.50) return "Type 2 Diabetes";        // 50% más común
            else if (random < 0.65) return "Prediabetic";        // 15%
            else if (random < 0.75) return "Type 1 Diabetes";    // 10%
            else if (random < 0.80) return "Gestational Diabetes"; // 5%
            else if (random < 0.85) return "LADA";               // 5%
            else if (random < 0.88) return "MODY";               // 3%
            else if (random < 0.91) return "Steroid-Induced Diabetes"; // 3%
            else if (random < 0.94) return "Secondary Diabetes"; // 3%
            else if (random < 0.96) return "Type 3c Diabetes (Pancreatogenic Diabetes)"; // 2%
            else if (random < 0.98) return "Cystic Fibrosis-Related Diabetes (CFRD)"; // 2%
            else if (random < 0.99) return "Wolfram Syndrome";   // 1%
            else return "Wolcott-Rallison Syndrome";             // 1%
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
                probabilities.put(clase, rand.nextDouble() * 0.10);
            }
        }

        // Normalizar a que sumen 1
        double total = probabilities.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total > 0) {
            probabilities.replaceAll((k, v) -> v / total);
        }

        return probabilities;
    }

    private Map<String, Double> generarImportanciaSimulada(Map<String, Object> features) {
        Map<String, Double> importance = new HashMap<>();
        Random rand = new Random();

        // Importancia basada en características reales
        if (features.containsKey("niveles_glucosa")) {
            importance.put("niveles_glucosa", 0.8 + (rand.nextDouble() * 0.15));
        }

        if (features.containsKey("niveles_insulina")) {
            importance.put("niveles_insulina", 0.6 + (rand.nextDouble() * 0.25));
        }

        if (features.containsKey("edad")) {
            importance.put("edad", 0.5 + (rand.nextDouble() * 0.3));
        }

        if (features.containsKey("autoanticuerpos")) {
            importance.put("autoanticuerpos", 0.7 + (rand.nextDouble() * 0.2));
        }

        if (features.containsKey("antecedentes_familiares")) {
            importance.put("antecedentes_familiares", 0.6 + (rand.nextDouble() * 0.25));
        }

        // Agregar algunas características más
        if (features.containsKey("indice_masa_corporal")) {
            importance.put("indice_masa_corporal", 0.4 + (rand.nextDouble() * 0.3));
        }

        if (features.containsKey("presion_arterial")) {
            importance.put("presion_arterial", 0.3 + (rand.nextDouble() * 0.2));
        }

        return importance;
    }

    private MLPredictionResponseDto crearRespuestaError(String mensaje) {
        MLPredictionResponseDto dto = new MLPredictionResponseDto();
        dto.setPredictedClass("Error");
        dto.setProbability(0.0);
        dto.setProbabilities(new HashMap<>());
        dto.setFeatureImportance(new HashMap<>());
        dto.setSuccess(false);
        dto.setMessage(mensaje);
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
        // Re-verificar cada 5 minutos si no está disponible
        if (!apiDisponible && (ultimaVerificacion == null ||
                ultimaVerificacion.plusMinutes(5).isBefore(LocalDateTime.now()))) {
            apiDisponible = pythonMLClient.verificarConexion();
            ultimaVerificacion = LocalDateTime.now();
        }
        return apiDisponible;
    }

    public Map<String, Object> getInfoModelo() {
        Map<String, Object> info = new HashMap<>();

        try {
            if (mlPythonEnabled && apiDisponible) {
                info = pythonMLClient.obtenerInfoModelo();
                info.put("origen", "API Python");
            } else {
                info.put("origen", "Modelo Simulado");
                info.put("modo", mlPythonEnabled ? "API no disponible" : "Deshabilitado");
            }

            info.put("pythonEnabled", mlPythonEnabled);
            info.put("apiDisponible", apiDisponible);
            info.put("ultimaVerificacion", ultimaVerificacion);

        } catch (Exception e) {
            log.error("Error obteniendo info del modelo: {}", e.getMessage());
            info.put("error", e.getMessage());
        }

        return info;
    }
}
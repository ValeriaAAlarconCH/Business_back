package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.MLPredictionResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PythonMLClient {

    @Value("${ml.python.api.url:http://localhost:5000}")
    private String pythonApiUrl;

    @Value("${ml.python.api.timeout:5000}")
    private int timeout;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PythonMLClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public boolean verificarConexion() {
        try {
            String url = pythonApiUrl + "/health";
            log.info("🔍 Verificando conexión con API Python: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Boolean modelLoaded = (Boolean) body.get("model_loaded");

                if (modelLoaded != null && modelLoaded) {
                    log.info("✅ API Python conectada. Modelo cargado correctamente");
                    log.info("📊 Número de features: {}", body.get("num_features"));
                    log.info("🎯 Clases disponibles: {}", body.get("classes"));
                    return true;
                } else {
                    log.warn("⚠️ API Python conectada pero modelo NO cargado");
                    return false;
                }
            }
        } catch (ResourceAccessException e) {
            log.error("❌ No se puede conectar a API Python en {}", pythonApiUrl);
            log.error("   Verifica que: 1) La API esté ejecutándose, 2) Puerto 5000 libre");
        } catch (Exception e) {
            log.error("❌ Error verificando conexión Python: {}", e.getMessage());
        }
        return false;
    }

    public MLPredictionResponseDto predecirConPython(Map<String, Object> features) {
        try {
            String url = pythonApiUrl + "/predict";
            log.info("📡 Llamando a API Python para predicción: {}", url);

            log.debug("📤 Enviando {} características al modelo:", features.size());
            features.forEach((key, value) ->
                    log.debug("   - {}: {}", key, value));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(features, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                Boolean success = (Boolean) responseBody.get("success");
                if (success == null || !success) {
                    String errorMsg = (String) responseBody.get("error");
                    log.error("❌ API Python reportó error: {}", errorMsg);
                    throw new RuntimeException("Error en API Python: " + errorMsg);
                }

                MLPredictionResponseDto dto = convertirRespuestaADto(responseBody);

                log.info("✅ Predicción exitosa: {} ({}%)",
                        dto.getPredictedClass(),
                        String.format("%.1f", dto.getProbability() * 100));

                return dto;

            } else {
                throw new RuntimeException("Respuesta HTTP no exitosa: " + response.getStatusCode());
            }

        } catch (ResourceAccessException e) {
            log.error("❌ No se puede conectar a API Python. ¿Está ejecutándose?");
            throw new RuntimeException("API Python no disponible. Error: " + e.getMessage());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("❌ Error HTTP {} de API Python: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Error en API Python: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("❌ Error inesperado llamando a API Python: {}", e.getMessage());
            throw new RuntimeException("Error llamando a API Python: " + e.getMessage());
        }
    }

    public MLPredictionResponseDto pruebaPrediccion() {
        try {
            Map<String, Object> datosPrueba = new HashMap<>();
            datosPrueba.put("edad", 45);
            datosPrueba.put("niveles_glucosa", 180.0);
            datosPrueba.put("niveles_insulina", 35.0);
            datosPrueba.put("indice_masa_corporal", 28.5);
            datosPrueba.put("autoanticuerpos", "Negative");
            datosPrueba.put("antecedentes_familiares", "Yes");
            datosPrueba.put("presion_arterial", 130.0);
            datosPrueba.put("niveles_colesterol", 220.0);

            log.info("🧪 Realizando prueba de predicción con datos de ejemplo...");
            return predecirConPython(datosPrueba);

        } catch (Exception e) {
            log.error("❌ Error en prueba de predicción: {}", e.getMessage());
            return null;
        }
    }

    public Map<String, Object> obtenerInfoModelo() {
        try {
            String url = pythonApiUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Error obteniendo info del modelo: {}", e.getMessage());
        }
        return new HashMap<>();
    }

    private MLPredictionResponseDto convertirRespuestaADto(Map<String, Object> responseBody) {
        MLPredictionResponseDto dto = new MLPredictionResponseDto();

        try {
            dto.setPredictedClass((String) responseBody.get("predictedClass"));

            dto.setProbability(convertirADouble(responseBody.get("probability")));

            if (responseBody.get("probabilities") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> probsMap = (Map<String, Object>) responseBody.get("probabilities");
                Map<String, Double> probabilidades = new HashMap<>();

                probsMap.forEach((clase, prob) -> {
                    probabilidades.put(clase, convertirADouble(prob));
                });
                dto.setProbabilities(probabilidades);
            }

            if (responseBody.get("featureImportance") instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> importanceMap = (Map<String, Object>) responseBody.get("featureImportance");
                Map<String, Double> importancia = new HashMap<>();

                importanceMap.forEach((feature, importanceVal) -> {
                    importancia.put(feature, convertirADouble(importanceVal));
                });
                dto.setFeatureImportance(importancia);
            }

        } catch (Exception e) {
            log.error("Error convirtiendo respuesta Python a DTO: {}", e.getMessage());
            throw new RuntimeException("Error procesando respuesta de API Python");
        }

        return dto;
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
}
package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.MLPredictionResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PythonMLClient {

    @Value("${ml.python.api.url:http://localhost:5000}")
    private String pythonApiUrl;

    @Value("${ml.python.api.timeout:10000}")
    private int timeout;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Constructor con inyección de RestTemplate
    public PythonMLClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        configurarRestTemplate();
    }

    private void configurarRestTemplate() {
        // Configurar timeout si es necesario
        // org.springframework.boot.web.client.RestTemplateBuilder podría usarse
    }

    public boolean verificarConexion() {
        try {
            String url = pythonApiUrl + "/health";
            log.info("🔍 Verificando conexión con API Python: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String body = response.getBody();
                log.debug("Respuesta health: {}", body);

                // Parsear la respuesta para verificar
                try {
                    Map<?, ?> jsonResponse = objectMapper.readValue(body, Map.class);
                    boolean modelLoaded = Boolean.TRUE.equals(jsonResponse.get("model_loaded"));
                    log.info("✅ API Python disponible. Modelo cargado: {}", modelLoaded);
                    return modelLoaded;
                } catch (JsonProcessingException e) {
                    log.warn("Respuesta no JSON válida, pero API responde");
                    return true; // Si responde, asumimos que funciona
                }
            }
            log.warn("API Python responde con código: {}", response.getStatusCode());
            return false;
        } catch (ResourceAccessException e) {
            log.warn("⏰ Timeout o no se puede conectar a API Python: {}", e.getMessage());
            return false;
        } catch (HttpClientErrorException e) {
            log.warn("API Python error HTTP: {} - {}", e.getStatusCode(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("❌ Error verificando conexión API Python: {}", e.getMessage());
            return false;
        }
    }

    public MLPredictionResponseDto predecirConPython(Map<String, Object> features) {
        try {
            String url = pythonApiUrl + "/predict";
            log.info("📡 Enviando predicción a API Python: {}", url);
            log.debug("Features enviados: {}", features);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(features, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.debug("Respuesta completa de API Python: {}", responseBody);

                // Convertir la respuesta a DTO
                MLPredictionResponseDto result = convertirRespuestaADto(responseBody);

                if (result != null && result.getPredictedClass() != null) {
                    log.info("✅ Predicción recibida: {} ({:.1f}%)",
                            result.getPredictedClass(),
                            result.getProbability() != null ? result.getProbability() * 100 : 0);
                    return result;
                } else {
                    throw new RuntimeException("Respuesta de API incompleta o inválida");
                }
            } else {
                throw new RuntimeException("API Python respondió con código: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("❌ Error HTTP de API Python ({}): {}", e.getStatusCode(), errorBody);
            throw new RuntimeException("Error en API Python: " + e.getStatusCode() + " - " + errorBody);
        } catch (ResourceAccessException e) {
            log.error("❌ No se puede acceder a API Python (timeout o conexión): {}", e.getMessage());
            throw new RuntimeException("No se puede conectar al servicio de ML. Verifique que la API esté corriendo en " + pythonApiUrl);
        } catch (Exception e) {
            log.error("❌ Error llamando a API Python: {}", e.getMessage(), e);
            throw new RuntimeException("Error al comunicarse con el servicio de ML: " + e.getMessage());
        }
    }

    private MLPredictionResponseDto convertirRespuestaADto(Map<String, Object> responseMap) {
        try {
            MLPredictionResponseDto dto = new MLPredictionResponseDto();

            // Mapear campos básicos
            dto.setPredictedClass((String) responseMap.get("predictedClass"));

            Object probability = responseMap.get("probability");
            if (probability != null) {
                if (probability instanceof Number) {
                    dto.setProbability(((Number) probability).doubleValue());
                } else if (probability instanceof String) {
                    dto.setProbability(Double.parseDouble((String) probability));
                }
            }

            dto.setSuccess((Boolean) responseMap.get("success"));
            dto.setMessage((String) responseMap.get("message"));

            // Mapear probabilidades
            Object probabilities = responseMap.get("probabilities");
            if (probabilities instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> probsMap = (Map<String, Object>) probabilities;
                Map<String, Double> probs = new HashMap<>();

                probsMap.forEach((key, value) -> {
                    if (value instanceof Number) {
                        probs.put(key, ((Number) value).doubleValue());
                    }
                });
                dto.setProbabilities(probs);
            }

            // Mapear importancia de features
            Object featureImportance = responseMap.get("featureImportance");
            if (featureImportance instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> featImpMap = (Map<String, Object>) featureImportance;
                Map<String, Double> featImp = new HashMap<>();

                featImpMap.forEach((key, value) -> {
                    if (value instanceof Number) {
                        featImp.put(key, ((Number) value).doubleValue());
                    }
                });
                dto.setFeatureImportance(featImp);
            }

            return dto;

        } catch (Exception e) {
            log.error("Error convirtiendo respuesta de API Python: {}", e.getMessage());
            return null;
        }
    }

    public MLPredictionResponseDto pruebaPrediccion() {
        try {
            log.info("🧪 Realizando prueba de predicción...");

            // Usar el endpoint /test de tu API Flask
            String url = pythonApiUrl + "/test";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Enviar POST vacío (la API Flask maneja datos de ejemplo internamente)
            HttpEntity<String> entity = new HttpEntity<>("{}", headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return convertirRespuestaADto(response.getBody());
            }

            log.error("❌ Prueba de predicción falló: respuesta vacía");
            return null;

        } catch (Exception e) {
            log.error("❌ Error en prueba de predicción: {}", e.getMessage(), e);
            return null;
        }
    }

    public Map<String, Object> obtenerInfoModelo() {
        try {
            String url = pythonApiUrl + "/features";
            log.info("📊 Obteniendo información del modelo de: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> info = response.getBody();
                log.info("✅ Información del modelo obtenida correctamente");
                return info != null ? info : new HashMap<>();
            }

            return new HashMap<>();

        } catch (Exception e) {
            log.warn("⚠️ No se pudo obtener información del modelo: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    public Map<String, Object> obtenerConfiguracion() {
        try {
            String url = pythonApiUrl + "/config";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            return new HashMap<>();

        } catch (Exception e) {
            log.warn("No se pudo obtener configuración: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
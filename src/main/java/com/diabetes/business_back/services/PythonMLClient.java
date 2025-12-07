package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.MLPredictionResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PythonMLClient {

    @Value("${ml.python.api.url:http://localhost:5000}")
    private String pythonApiUrl;

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

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                if (body != null) {
                    Object modelLoaded = body.get("model_loaded");
                    log.info("✅ API Python disponible: {}", modelLoaded);
                    return Boolean.TRUE.equals(modelLoaded);
                }
            }
            return false;
        } catch (Exception e) {
            log.error("❌ No se puede conectar a API Python: {}", e.getMessage());
            return false;
        }
    }

    public MLPredictionResponseDto predecirConPython(Map<String, Object> features) {
        try {
            String url = pythonApiUrl + "/predict";
            log.info("📡 Enviando predicción a API Python con {} features", features.size());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(features, headers);

            ResponseEntity<MLPredictionResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    MLPredictionResponseDto.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                MLPredictionResponseDto result = response.getBody();
                log.info("✅ Predicción recibida: {} ({:.1f}%)",
                        result.getPredictedClass(),
                        result.getProbability() * 100);
                return result;
            } else {
                throw new RuntimeException("Respuesta no válida de la API Python");
            }
        } catch (Exception e) {
            log.error("❌ Error llamando a API Python: {}", e.getMessage());
            throw new RuntimeException("Error al comunicarse con el servicio de ML: " + e.getMessage());
        }
    }

    public MLPredictionResponseDto pruebaPrediccion() {
        try {
            Map<String, Object> testData = new HashMap<>();
            testData.put("edad", 45);
            testData.put("niveles_glucosa", 180.0);
            testData.put("niveles_insulina", 35.0);
            testData.put("indice_masa_corporal", 28.5);
            testData.put("autoanticuerpos", "Negative");
            testData.put("antecedentes_familiares", "Yes");
            testData.put("presion_arterial", 130.0);
            testData.put("niveles_colesterol", 220.0);

            return predecirConPython(testData);
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
                Map<String, Object> body = response.getBody();
                if (body != null) {
                    log.info("📊 Información del modelo obtenida: {} features",
                            body.get("num_features"));
                }
                return body;
            }
            return new HashMap<>();
        } catch (Exception e) {
            log.error("❌ Error obteniendo info del modelo: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
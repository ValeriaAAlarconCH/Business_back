package com.diabetes.business_back.controllers;

import com.diabetes.business_back.entities.TipoDiabetesInfo;
import com.diabetes.business_back.repositories.TipoDiabetesInfoRepository;
import com.diabetes.business_back.services.ModeloMLService;
import com.diabetes.business_back.services.PythonMLClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200",
        allowCredentials = "true",
        exposedHeaders = "Authorization")
@RequestMapping("/ml-integration")
public class MLIntegrationController {
    @Autowired
    private TipoDiabetesInfoRepository tipoDiabetesInfoRepository;

    @Autowired
    private ModeloMLService modeloMLService;

    @Autowired
    private PythonMLClient pythonMLClient;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean apiDisponible = modeloMLService.isAPIDisponible();
            Map<String, Object> infoModelo = modeloMLService.getInfoModelo();

            response.put("apiPythonDisponible", apiDisponible);
            response.put("infoModelo", infoModelo);
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "OK");

            log.info("📊 Estado de integración ML: API Python disponible = {}", apiDisponible);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error obteniendo estado de integración ML: {}", e.getMessage());
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/test-prediction")
    public ResponseEntity<Map<String, Object>> testPrediction() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("🧪 Ejecutando prueba de predicción...");

            // Datos de prueba
            Map<String, Object> testData = new HashMap<>();
            testData.put("edad", 45);
            testData.put("niveles_glucosa", 180.0);
            testData.put("niveles_insulina", 35.0);
            testData.put("indice_masa_corporal", 28.5);
            testData.put("autoanticuerpos", "Negative");
            testData.put("antecedentes_familiares", "Yes");

            // Realizar predicción de prueba
            var prediction = pythonMLClient.pruebaPrediccion();

            if (prediction != null) {
                response.put("status", "SUCCESS");
                response.put("prediccion", prediction);
                response.put("message", "Prueba de predicción exitosa");
                log.info("✅ Prueba de predicción exitosa: {}", prediction.getPredictedClass());
            } else {
                response.put("status", "ERROR");
                response.put("message", "La prueba de predicción falló");
                log.error("❌ Prueba de predicción falló");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error en prueba de predicción: {}", e.getMessage());
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean pythonApiHealthy = pythonMLClient.verificarConexion();

            response.put("service", "diabetes-ml-integration");
            response.put("status", "UP");
            response.put("pythonApiAvailable", pythonApiHealthy);
            response.put("timestamp", System.currentTimeMillis());

            if (pythonApiHealthy) {
                response.put("message", "✅ Servicio ML integrado correctamente");
            } else {
                response.put("message", "⚠️ API Python no disponible - Usando modo simulado");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Health check failed: {}", e.getMessage());
            response.put("service", "diabetes-ml-integration");
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/tipos-disponibles")
    public ResponseEntity<Map<String, Object>> getTiposDisponibles() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Tipos que tu modelo DEBE poder predecir
            List<String> tiposEsperados = Arrays.asList(
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

            // Obtener tipos de la base de datos
            List<TipoDiabetesInfo> tiposBD = tipoDiabetesInfoRepository.findAll();
            List<String> tiposEnBD = tiposBD.stream()
                    .map(TipoDiabetesInfo::getNombreEn)
                    .collect(Collectors.toList());

            // Verificar coincidencias
            List<String> tiposFaltantes = new ArrayList<>();
            List<String> tiposExtra = new ArrayList<>();

            for (String esperado : tiposEsperados) {
                if (!tiposEnBD.contains(esperado)) {
                    tiposFaltantes.add(esperado);
                }
            }

            for (String enBD : tiposEnBD) {
                if (!tiposEsperados.contains(enBD)) {
                    tiposExtra.add(enBD);
                }
            }

            response.put("tipos_esperados", tiposEsperados);
            response.put("tipos_en_bd", tiposEnBD);
            response.put("total_esperado", tiposEsperados.size());
            response.put("total_en_bd", tiposEnBD.size());
            response.put("tipos_faltantes", tiposFaltantes);
            response.put("tipos_extra", tiposExtra);
            response.put("coincide_completamente", tiposFaltantes.isEmpty() && tiposExtra.isEmpty());
            response.put("status", tiposFaltantes.isEmpty() ? "OK" : "INCOMPLETO");

            if (!tiposFaltantes.isEmpty()) {
                response.put("mensaje", "Faltan tipos de diabetes en la base de datos");
                log.warn("⚠️ Faltan tipos de diabetes en BD: {}", tiposFaltantes);
            } else if (!tiposExtra.isEmpty()) {
                response.put("mensaje", "Hay tipos adicionales en la base de datos");
                log.info("📊 Tipos adicionales en BD: {}", tiposExtra);
            } else {
                response.put("mensaje", "Todos los tipos están correctamente registrados");
                log.info("✅ Todos los 12 tipos de diabetes están registrados en BD");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error verificando tipos disponibles: {}", e.getMessage());
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
package com.diabetes.business_back.controllers;

import com.diabetes.business_back.services.ModeloMLService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Sistema Principal", description = "Endpoints generales del sistema")
public class MainController {

    @Value("${spring.application.name:Diabetes Prediction System}")
    private String appName;

    @Value("${server.port:8080}")
    private String port;

    @Autowired
    private ModeloMLService modeloMLService;

    @GetMapping("/health")
    @Operation(summary = "Verificar estado del sistema")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("📡 Health check solicitado");

        Map<String, Object> response = new HashMap<>();
        response.put("application", appName);
        response.put("status", "UP");
        response.put("port", port);
        response.put("timestamp", System.currentTimeMillis());

        // Verificar si ML Service está disponible
        boolean mlAvailable = false;
        try {
            mlAvailable = modeloMLService.isAPIDisponible();
        } catch (Exception e) {
            log.warn("Error verificando ML service: {}", e.getMessage());
        }

        response.put("mlService", mlAvailable ? "CONNECTED" : "DISCONNECTED");
        response.put("version", "1.0.0");
        response.put("environment", "development");

        log.info("✅ Health check: ML Service = {}", mlAvailable ? "CONNECTED" : "DISCONNECTED");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/docs")
    @Operation(summary = "Información de documentación de la API")
    public ResponseEntity<Map<String, Object>> getDocsInfo() {
        Map<String, Object> response = new HashMap<>();

        response.put("swagger-ui", "http://localhost:" + port + "/swagger-ui.html");
        response.put("api-docs", "http://localhost:" + port + "/v3/api-docs");
        response.put("openapi", "http://localhost:" + port + "/v3/api-docs.yaml");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
                "health", "GET /api/health",
                "pacientes", "GET/POST /api/pacientes/**",
                "evaluaciones", "GET/POST /api/evaluaciones/**",
                "predicciones", "POST /api/evaluaciones/predecir",
                "ml-integration", "GET /api/ml-integration/**",
                "guias-campos", "GET /api/guias-campos/**",
                "tipos-diabetes", "GET /api/tipos-diabetes/**"
        ));

        return ResponseEntity.ok(response);
    }

    // Endpoint de prueba adicional
    @GetMapping("/ping")
    @Operation(summary = "Prueba simple de conexión")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }
}
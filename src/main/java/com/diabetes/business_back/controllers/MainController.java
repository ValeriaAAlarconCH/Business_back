package com.diabetes.business_back.controllers;

import com.diabetes.business_back.services.ModeloMLService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Sistema Principal", description = "Endpoints generales del sistema")
public class MainController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${server.port}")
    private String port;

    @Autowired
    private ModeloMLService modeloMLService;

    @GetMapping("/health")
    @Operation(summary = "Verificar estado del sistema")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();

        response.put("application", appName);
        response.put("status", "UP");
        response.put("port", port);
        response.put("contextPath", contextPath);
        response.put("timestamp", System.currentTimeMillis());
        response.put("mlService", modeloMLService.isAPIDisponible() ? "CONNECTED" : "DISCONNECTED");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/docs")
    @Operation(summary = "Información de documentación de la API")
    public ResponseEntity<Map<String, Object>> getDocsInfo() {
        Map<String, Object> response = new HashMap<>();

        response.put("swagger-ui", "http://localhost:" + port + contextPath + "/swagger-ui/index.html");
        response.put("api-docs", "http://localhost:" + port + contextPath + "/v3/api-docs");
        response.put("version", "1.0.0");
        response.put("endpoints", Map.of(
                "pacientes", "/api/pacientes/**",
                "evaluaciones", "/api/evaluaciones/**",
                "predicciones", "/api/evaluaciones/predecir",
                "ml", "/api/ml-integration/**",
                "guias", "/api/guias-campos/**",
                "tipos-diabetes", "/api/tipos-diabetes/**"
        ));

        return ResponseEntity.ok(response);
    }
}
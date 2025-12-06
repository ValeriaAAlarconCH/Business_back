package com.diabetes.business_back.controllers;

import com.diabetes.business_back.dtos.EvaluacionDiabetesDto;
import com.diabetes.business_back.dtos.EvaluacionRequestDto;
import com.diabetes.business_back.dtos.PrediccionResponseDto;
import com.diabetes.business_back.interfaces.IEvaluacionDiabetesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200",
        allowCredentials = "true",
        exposedHeaders = "Authorization")
@RequestMapping("/evaluaciones")
public class EvaluacionDiabetesController {
    @Autowired
    private IEvaluacionDiabetesService evaluacionservice;

    @PostMapping("/registrar")
    public ResponseEntity<EvaluacionDiabetesDto> evaluacion(@RequestBody EvaluacionDiabetesDto evaluaciondto) {
        return ResponseEntity.ok(evaluacionservice.grabarEvaluacion(evaluaciondto));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<EvaluacionDiabetesDto>> getEvaluaciones() {
        return ResponseEntity.ok(evaluacionservice.getEvaluaciones());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable("id") Long id) {
        evaluacionservice.eliminar(id);
        return ResponseEntity.ok("Evaluación eliminada correctamente");
    }

    @PutMapping("/actualizar")
    public ResponseEntity<EvaluacionDiabetesDto> actualizar(@RequestBody EvaluacionDiabetesDto evaluaciondto) {
        EvaluacionDiabetesDto actualizado = evaluacionservice.actualizar(evaluaciondto);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/listarid/{id}")
    public ResponseEntity<EvaluacionDiabetesDto> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(evaluacionservice.obtenerPorId(id));
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<EvaluacionDiabetesDto>> obtenerPorPaciente(@PathVariable("idPaciente") Long idPaciente) {
        return ResponseEntity.ok(evaluacionservice.obtenerEvaluacionesPorPaciente(idPaciente));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<EvaluacionDiabetesDto>> obtenerPorTipo(@PathVariable("tipo") String tipo) {
        return ResponseEntity.ok(evaluacionservice.obtenerEvaluacionesPorTipo(tipo));
    }

    @PostMapping("/predecir")
    public ResponseEntity<PrediccionResponseDto> predecir(@RequestBody EvaluacionRequestDto request) {
        return ResponseEntity.ok(evaluacionservice.realizarPrediccion(request));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticas() {
        return ResponseEntity.ok(evaluacionservice.obtenerEstadisticas());
    }
}

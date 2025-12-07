package com.diabetes.business_back.controllers;

import com.diabetes.business_back.dtos.PacienteDto;
import com.diabetes.business_back.interfaces.IPacienteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4200",
        allowCredentials = "true",
        exposedHeaders = "Authorization")
@RequestMapping("/api/pacientes")
public class PacienteController {
    @Autowired
    private IPacienteService pacienteservice;

    @PostMapping("/registrar")
    public ResponseEntity<PacienteDto> paciente(@RequestBody PacienteDto pacientedto) {
        return ResponseEntity.ok(pacienteservice.grabarPaciente(pacientedto));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<PacienteDto>> getPacientes() {
        return ResponseEntity.ok(pacienteservice.getPacientes());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable("id") Long id) {
        pacienteservice.eliminar(id);
        return ResponseEntity.ok("Paciente eliminado correctamente");
    }

    @PutMapping("/actualizar")
    public ResponseEntity<PacienteDto> actualizar(@RequestBody PacienteDto pacientedto) {
        PacienteDto actualizado = pacienteservice.actualizar(pacientedto);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/listarid/{id}")
    public ResponseEntity<PacienteDto> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(pacienteservice.obtenerPorId(id));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<PacienteDto> obtenerPorCodigo(@PathVariable("codigo") String codigo) {
        return ResponseEntity.ok(pacienteservice.obtenerPorCodigo(codigo));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<PacienteDto> obtenerPorEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(pacienteservice.obtenerPorEmail(email));
    }
}
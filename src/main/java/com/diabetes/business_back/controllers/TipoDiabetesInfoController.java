package com.diabetes.business_back.controllers;

import com.diabetes.business_back.dtos.TipoDiabetesInfoDto;
import com.diabetes.business_back.interfaces.ITipoDiabetesInfoService;
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
@RequestMapping("/api/tipos-diabetes")
public class TipoDiabetesInfoController {
    @Autowired
    private ITipoDiabetesInfoService tipodiabetesservice;

    @PostMapping("/registrar")
    public ResponseEntity<TipoDiabetesInfoDto> tipoDiabetes(@RequestBody TipoDiabetesInfoDto tipodto) {
        return ResponseEntity.ok(tipodiabetesservice.grabarTipoDiabetes(tipodto));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<TipoDiabetesInfoDto>> getTiposDiabetes() {
        return ResponseEntity.ok(tipodiabetesservice.getTiposDiabetes());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable("id") Long id) {
        tipodiabetesservice.eliminar(id);
        return ResponseEntity.ok("Tipo de diabetes eliminado correctamente");
    }

    @PutMapping("/actualizar")
    public ResponseEntity<TipoDiabetesInfoDto> actualizar(@RequestBody TipoDiabetesInfoDto tipodto) {
        TipoDiabetesInfoDto actualizado = tipodiabetesservice.actualizar(tipodto);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/listarid/{id}")
    public ResponseEntity<TipoDiabetesInfoDto> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(tipodiabetesservice.obtenerPorId(id));
    }

    @GetMapping("/nombre/{nombreEn}")
    public ResponseEntity<TipoDiabetesInfoDto> obtenerPorNombre(@PathVariable("nombreEn") String nombreEn) {
        return ResponseEntity.ok(tipodiabetesservice.obtenerPorNombreEn(nombreEn));
    }

    @GetMapping("/comunes")
    public ResponseEntity<List<TipoDiabetesInfoDto>> obtenerComunes() {
        return ResponseEntity.ok(tipodiabetesservice.obtenerTiposComunes());
    }
}
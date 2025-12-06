package com.diabetes.business_back.controllers;

import com.diabetes.business_back.dtos.GuiaCampoDto;
import com.diabetes.business_back.interfaces.IGuiaCampoService;
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
@RequestMapping("/guias-campos")
public class GuiaCampoController {

    @Autowired
    private IGuiaCampoService guiacamposervice;

    @PostMapping("/registrar")
    public ResponseEntity<GuiaCampoDto> guiaCampo(@RequestBody GuiaCampoDto guiadto) {
        return ResponseEntity.ok(guiacamposervice.grabarGuiaCampo(guiadto));
    }

    @GetMapping("/listar")
    public ResponseEntity<List<GuiaCampoDto>> getGuiasCampos() {
        return ResponseEntity.ok(guiacamposervice.getGuiasCampos());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable("id") Long id) {
        guiacamposervice.eliminar(id);
        return ResponseEntity.ok("Guía de campo eliminada correctamente");
    }

    @PutMapping("/actualizar")
    public ResponseEntity<GuiaCampoDto> actualizar(@RequestBody GuiaCampoDto guiadto) {
        GuiaCampoDto actualizado = guiacamposervice.actualizar(guiadto);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/listarid/{id}")
    public ResponseEntity<GuiaCampoDto> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(guiacamposervice.obtenerPorId(id));
    }

    @GetMapping("/campo/{nombreCampo}")
    public ResponseEntity<GuiaCampoDto> obtenerPorNombreCampo(@PathVariable("nombreCampo") String nombreCampo) {
        return ResponseEntity.ok(guiacamposervice.obtenerPorNombreCampo(nombreCampo));
    }
}

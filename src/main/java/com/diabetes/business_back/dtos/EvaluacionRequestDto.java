package com.diabetes.business_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionRequestDto implements Serializable {
    private PacienteDto pacientedto;

    private String marcadoresGeneticos;
    private String autoanticuerpos;
    private String antecedentesFamiliares;
    private String factoresAmbientales;
    private String etnicidad;
    private String habitosAlimenticios;
    private String pruebaToleranciaGlucosa;
    private String pruebasFuncionHepatica;
    private String diagnosticoFibrosisQuistica;
    private String usoEsteroides;
    private String pruebasGeneticas;
    private String historialEmbarazos;
    private String diabetesGestacionalPrevia;
    private String historialPcos;
    private String estadoTabaquismo;
    private String sintomasInicioTemprano;
    private String factoresSocioeconomicos;
    private String consumoAlcohol;
    private String actividadFisica;
    private String pruebaOrina;

    private Double nivelesInsulina;
    private Integer edad;
    private Double indiceMasaCorporal;
    private Double presionArterial;
    private Double nivelesColesterol;
    private Double circunferenciaCintura;
    private Double nivelesGlucosa;
    private Double aumentoPesoEmbarazo;
    private Double saludPancreatica;
    private Double funcionPulmonar;
    private Double evaluacionesNeurologicas;
    private Double nivelesEnzimasDigestivas;
    private Double pesoNacimiento;
}
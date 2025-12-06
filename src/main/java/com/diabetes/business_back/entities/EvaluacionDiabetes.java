package com.diabetes.business_back.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evaluaciones_diabetes")
public class EvaluacionDiabetes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Long idEvaluacion;
    @ManyToOne
    @JoinColumn(name = "id_paciente")
    private Paciente paciente;

    private LocalDateTime fechaEvaluacion;
    private String tipoDiabetesPredicho;
    private Double probabilidad;
    private String explicacion;
    private String recomendaciones;

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

    private String clasificacionPresion;
    private String clasificacionColesterol;
    private String clasificacionInsulina;
    private String clasificacionGlucosa;
    private String clasificacionEnzimas;
    private String clasificacionEdad;
}
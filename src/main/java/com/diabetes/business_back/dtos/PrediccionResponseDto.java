package com.diabetes.business_back.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrediccionResponseDto implements Serializable {
    private String tipoDiabetes;
    private String tipoDiabetesEs;
    private Double probabilidad;
    private String explicacion;
    private Map<String, String> clasificaciones;
    private TipoDiabetesInfoDto informacionTipo;
    private String recomendacionesPersonalizadas;
    private LocalDateTime fechaPrediccion;
}
package com.diabetes.business_back.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class TipoDiabetesInfoDto implements Serializable {
    private Long idTipoDiabetes;
    private String nombreEn;
    private String nombreEs;
    private String descripcion;
    private String causas;
    private String sintomas;
    private String tratamiento;
    private String recomendaciones;
    private Boolean esComun;
}
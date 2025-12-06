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

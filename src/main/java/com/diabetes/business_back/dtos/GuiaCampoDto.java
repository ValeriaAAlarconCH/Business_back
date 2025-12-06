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
public class GuiaCampoDto implements Serializable {
    private Long idGuia;
    private String nombreCampo;
    private String tituloEs;
    private String descripcionEs;
    private String ejemplos;
    private String rangoRecomendado;
    private String unidadMedida;
}
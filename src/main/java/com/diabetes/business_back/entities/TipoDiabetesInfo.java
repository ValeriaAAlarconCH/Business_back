package com.diabetes.business_back.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tipos_diabetes_info")
public class TipoDiabetesInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_diabetes")
    private Long idTipoDiabetes;
    @Column(unique = true)
    private String nombreEn;
    private String nombreEs;
    @Column(length = 2000)
    private String descripcion;
    @Column(length = 1000)
    private String causas;
    @Column(length = 1000)
    private String sintomas;
    @Column(length = 1000)
    private String tratamiento;
    @Column(length = 1000)
    private String recomendaciones;
    private Boolean esComun;
}
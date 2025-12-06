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
@Table(name = "guias_campos")
public class GuiaCampo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_guia")
    private Long idGuia;

    @Column(unique = true)
    private String nombreCampo;

    private String tituloEs;

    @Column(length = 500)
    private String descripcionEs;

    @Column(length = 200)
    private String ejemplos;

    private String rangoRecomendado;
    private String unidadMedida;
}

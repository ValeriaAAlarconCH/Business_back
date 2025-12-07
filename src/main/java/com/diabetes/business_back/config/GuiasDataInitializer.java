package com.diabetes.business_back.config;

import com.diabetes.business_back.entities.GuiaCampo;
import com.diabetes.business_back.repositories.GuiaCampoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class GuiasDataInitializer {
    @Bean
    public CommandLineRunner initGuiasData(GuiaCampoRepository guiaCampoRepository) {
        return args -> {
            if (guiaCampoRepository.count() == 0) {
                log.info("📝 Insertando guías de campos iniciales...");

                List<GuiaCampo> guias = Arrays.asList(
                        crearGuia("edad", "Edad del Paciente",
                                "Edad del paciente en años completos. Es un factor crucial en el diagnóstico de diferentes tipos de diabetes.",
                                "45, 30, 60, 12", "0-120 años", "años"),

                        crearGuia("niveles_glucosa", "Niveles de Glucosa en Sangre",
                                "Medición de glucosa en sangre en ayunas. Valores elevados indican posible diabetes.",
                                "100, 180, 125, 85", "70-125 mg/dL", "mg/dL"),

                        crearGuia("niveles_insulina", "Niveles de Insulina",
                                "Medición de insulina en sangre. Ayuda a determinar resistencia a la insulina.",
                                "15, 35, 50, 8", "2.6-24.9 μIU/mL", "μIU/mL"),

                        crearGuia("autoanticuerpos", "Autoanticuerpos Pancreáticos",
                                "Presencia de autoanticuerpos que atacan células beta del páncreas. Marcador de diabetes autoinmune.",
                                "Positive, Negative", "Negative/Positive", ""),

                        crearGuia("antecedentes_familiares", "Antecedentes Familiares de Diabetes",
                                "Historia de diabetes en familiares de primer grado (padres, hermanos).",
                                "Yes, No", "Yes/No", ""),

                        crearGuia("indice_masa_corporal", "Índice de Masa Corporal (IMC)",
                                "Relación entre peso y altura. Indica estado nutricional.",
                                "24.5, 30.2, 18.8, 32.0", "18.5-24.9 kg/m²", "kg/m²"),

                        crearGuia("presion_arterial", "Presión Arterial Sistólica",
                                "Presión arterial sistólica (la superior). La hipertensión es común en diabetes.",
                                "120, 130, 140, 110", "90-130 mmHg", "mmHg"),

                        crearGuia("niveles_colesterol", "Niveles de Colesterol Total",
                                "Colesterol total en sangre. La diabetes aumenta riesgo cardiovascular.",
                                "180, 220, 240, 190", "<200 mg/dL", "mg/dL")
                );

                guiaCampoRepository.saveAll(guias);
                log.info("✅ {} guías de campo insertadas", guias.size());
            }
        };
    }

    private GuiaCampo crearGuia(String nombreCampo, String tituloEs, String descripcionEs,
                                String ejemplos, String rangoRecomendado, String unidadMedida) {
        GuiaCampo guia = new GuiaCampo();
        guia.setNombreCampo(nombreCampo);
        guia.setTituloEs(tituloEs);
        guia.setDescripcionEs(descripcionEs);
        guia.setEjemplos(ejemplos);
        guia.setRangoRecomendado(rangoRecomendado);
        guia.setUnidadMedida(unidadMedida);
        return guia;
    }
}
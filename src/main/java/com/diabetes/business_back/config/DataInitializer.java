package com.diabetes.business_back.config;

import com.diabetes.business_back.entities.TipoDiabetesInfo;
import com.diabetes.business_back.repositories.TipoDiabetesInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner initData(TipoDiabetesInfoRepository tipoDiabetesInfoRepository) {
        return args -> {
            if (tipoDiabetesInfoRepository.count() == 0) {
                log.info("📝 Insertando datos iniciales de TODOS los tipos de diabetes...");

                List<TipoDiabetesInfo> tipos = Arrays.asList(
                        crearTipoDiabetes("Steroid-Induced Diabetes", "Diabetes Inducida por Esteroides",
                                "Forma de diabetes causada por el uso prolongado de glucocorticoides que afectan la sensibilidad a la insulina.",
                                "Uso de corticosteroides en altas dosis o por tiempo prolongado.",
                                "Aumento de sed, micción frecuente, fatiga, visión borrosa durante tratamiento con esteroides.",
                                "Ajuste de dosis de esteroides si es posible, medicamentos antidiabéticos, posible insulina temporal.",
                                "Monitoreo de glucosa durante tratamientos con esteroides, educación sobre interacciones medicamentosas.", false),

                        crearTipoDiabetes("Prediabetic", "Prediabetes",
                                "Estado intermedio donde los niveles de glucosa son más altos de lo normal pero no lo suficiente para diagnosticar diabetes.",
                                "Sobrepeso, sedentarismo, dieta inadecuada, antecedentes familiares de diabetes.",
                                "Generalmente asintomática, puede haber fatiga leve o aumento de sed ocasional.",
                                "Cambios en estilo de vida, pérdida de peso del 5-10%, aumento de actividad física.",
                                "Control anual, dieta balanceada, ejercicio regular, prevención de progresión a diabetes tipo 2.", true),

                        crearTipoDiabetes("Type 1 Diabetes", "Diabetes Tipo 1",
                                "Enfermedad autoinmune donde el sistema inmunológico ataca y destruye las células beta del páncreas que producen insulina.",
                                "Factores genéticos y ambientales, posiblemente desencadenados por virus o factores autoinmunes.",
                                "Sed excesiva, hambre constante, micción frecuente, pérdida de peso inexplicable, fatiga extrema.",
                                "Insulina inyectable o por bomba de infusión, monitoreo continuo de glucosa, conteo de carbohidratos.",
                                "Control estricto de glucosa, educación diabetológica, chequeos médicos regulares, prevención de complicaciones.", true),

                        crearTipoDiabetes("Wolfram Syndrome", "Síndrome de Wolfram",
                                "Trastorno genético poco común que combina diabetes mellitus con atrofia óptica, pérdida de audición y problemas neurológicos.",
                                "Mutaciones en el gen WFS1, herencia autosómica recesiva.",
                                "Diabetes infantil, pérdida progresiva de visión, pérdida de audición, diabetes insípida, problemas neurológicos.",
                                "Insulina para la diabetes, tratamiento sintomático para problemas visuales y auditivos, manejo multidisciplinario.",
                                "Atención por equipo multidisciplinario, apoyo genético, seguimiento neurológico y oftalmológico regular.", false),

                        crearTipoDiabetes("LADA", "Diabetes Autoimmune Latente en Adultos",
                                "Variante autoinmune de diabetes que se presenta en adultos, con progresión más lenta que la tipo 1.",
                                "Autoinmunidad pancreática similar a diabetes tipo 1, factores genéticos, generalmente en adultos >30 años.",
                                "Síntomas similares a diabetes tipo 2 pero en personas delgadas, progresión gradual, presencia de autoanticuerpos.",
                                "Insulina eventualmente necesaria, posible uso de medicamentos orales en etapas iniciales, similar a tipo 1.",
                                "Pruebas de autoanticuerpos para diagnóstico, seguimiento endocrinológico estrecho, educación sobre insulinoterapia.", false),

                        crearTipoDiabetes("Type 2 Diabetes", "Diabetes Tipo 2",
                                "Forma más común de diabetes, caracterizada por resistencia a la insulina y disfunción progresiva de las células beta.",
                                "Obesidad, sedentarismo, dieta poco saludable, factores genéticos, edad avanzada.",
                                "Sed aumentada, hambre constante, micción frecuente, visión borrosa, fatiga, heridas que sanan lentamente.",
                                "Cambios en estilo de vida, medicamentos orales (metformina, sulfonilureas), posible insulina en etapas avanzadas.",
                                "Pérdida de peso, ejercicio regular, dieta saludable, monitoreo glucémico, prevención de complicaciones cardiovasculares.", true),

                        crearTipoDiabetes("Wolcott-Rallison Syndrome", "Síndrome de Wolcott-Rallison",
                                "Trastorno genético raro caracterizado por diabetes neonatal permanente, displasia epifisaria múltiple y disfunción hepática.",
                                "Mutaciones en el gen EIF2AK3, herencia autosómica recesiva.",
                                "Diabetes neonatal permanente, problemas esqueléticos (displasia epifisaria), trastornos hepáticos recurrentes.",
                                "Insulina desde edad temprana, manejo ortopédico de problemas esqueléticos, tratamiento de disfunción hepática.",
                                "Atención especializada multidisciplinaria, consejo genético, manejo neonatal intensivo, seguimiento hepático.", false),

                        crearTipoDiabetes("Secondary Diabetes", "Diabetes Secundaria",
                                "Diabetes que surge como consecuencia de otra enfermedad o condición médica o uso de ciertos medicamentos.",
                                "Enfermedades pancreáticas (pancreatitis), endocrinopatías (síndrome de Cushing), medicamentos (antipsicóticos).",
                                "Depende de la condición subyacente, generalmente incluye síntomas clásicos de diabetes.",
                                "Tratamiento de la condición subyacente, manejo glucémico con insulina o medicamentos según severidad.",
                                "Evaluación completa para identificar causa subyacente, manejo integral de condición primaria y diabetes.", false),

                        crearTipoDiabetes("Type 3c Diabetes (Pancreatogenic Diabetes)", "Diabetes Tipo 3c (Pancreatogénica)",
                                "Diabetes resultante de daño al páncreas exocrino, generalmente por pancreatitis crónica, cáncer o resección pancreática.",
                                "Pancreatitis crónica, cáncer de páncreas, cirugía pancreática, fibrosis quística, hemocromatosis.",
                                "Diabetes junto con síntomas de insuficiencia pancreática exocrina (esteatorrea, pérdida de peso, dolor abdominal).",
                                "Insulina (frecuentemente requerida), enzimas pancreáticas suplementarias, manejo nutricional especializado.",
                                "Seguimiento por gastroenterología y endocrinología, soporte nutricional, manejo del dolor, prevención de complicaciones.", false),

                        crearTipoDiabetes("Gestational Diabetes", "Diabetes Gestacional",
                                "Diabetes que se desarrolla durante el embarazo en mujeres que no tenían diabetes previamente.",
                                "Cambios hormonales del embarazo, predisposición genética, sobrepeso, edad materna avanzada.",
                                "Generalmente asintomática, detectada mediante pruebas de glucosa rutinarias durante el embarazo.",
                                "Control dietético, ejercicio moderado, posiblemente insulina si no se controla con dieta y ejercicio.",
                                "Monitoreo durante el embarazo, control posparto a las 6-12 semanas, prevención de diabetes tipo 2 futura.", true),

                        crearTipoDiabetes("Cystic Fibrosis-Related Diabetes (CFRD)", "Diabetes Relacionada con Fibrosis Quística",
                                "Diabetes asociada a fibrosis quística, resultante del daño progresivo al páncreas que afecta función endocrina y exocrina.",
                                "Fibrosis quística, destrucción pancreática progresiva por tapones de moco e inflamación.",
                                "Síntomas diabéticos junto con síntomas respiratorios y digestivos característicos de fibrosis quística.",
                                "Insulina (generalmente requerida), manejo nutricional intensivo, tratamiento agresivo de fibrosis quística.",
                                "Equipo multidisciplinario (endocrinólogo, neumólogo, nutricionista), monitorización estrecha, educación sobre insulinoterapia.", false),

                        crearTipoDiabetes("MODY", "MODY (Diabetes de la Madurez de Inicio Juvenil)",
                                "Forma monogénica de diabetes hereditaria, generalmente aparece antes de los 25 años, sigue patrón autosómico dominante.",
                                "Mutaciones genéticas específicas (HNF1A, HNF4A, GCK), herencia autosómica dominante.",
                                "Hiperglucemia leve a moderada, diagnóstico frecuente en jóvenes no obesos, fuerte historia familiar.",
                                "Depende del tipo de MODY: desde solo dieta hasta sulfonilureas o insulina, tratamiento personalizado según mutación.",
                                "Pruebas genéticas para confirmación y guía de tratamiento, seguimiento familiar, asesoramiento genético.", false)
                );

                tipoDiabetesInfoRepository.saveAll(tipos);
                log.info("TODOS los {} tipos de diabetes insertados correctamente", tipos.size());
                log.info("Tipos disponibles: Steroid-Induced Diabetes, Prediabetic, Type 1 Diabetes, Wolfram Syndrome, LADA, Type 2 Diabetes, Wolcott-Rallison Syndrome, Secondary Diabetes, Type 3c Diabetes, Gestational Diabetes, CFRD, MODY");

            } else {
                long count = tipoDiabetesInfoRepository.count();
                log.info("Ya existen {} tipos de diabetes en la base de datos", count);

                if (count < 12) {
                    log.warn("Faltan algunos tipos de diabetes. Deberían ser 12 pero hay {}", count);
                }
            }
        };
    }

    private TipoDiabetesInfo crearTipoDiabetes(String nombreEn, String nombreEs,
                                               String descripcion, String causas,
                                               String sintomas, String tratamiento,
                                               String recomendaciones, boolean esComun) {
        TipoDiabetesInfo tipo = new TipoDiabetesInfo();
        tipo.setNombreEn(nombreEn);
        tipo.setNombreEs(nombreEs);
        tipo.setDescripcion(descripcion);
        tipo.setCausas(causas);
        tipo.setSintomas(sintomas);
        tipo.setTratamiento(tratamiento);
        tipo.setRecomendaciones(recomendaciones);
        tipo.setEsComun(esComun);
        return tipo;
    }
}
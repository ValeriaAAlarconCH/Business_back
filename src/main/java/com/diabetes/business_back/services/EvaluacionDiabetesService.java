package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.*;
import com.diabetes.business_back.entities.EvaluacionDiabetes;
import com.diabetes.business_back.entities.Paciente;
import com.diabetes.business_back.entities.TipoDiabetesInfo;
import com.diabetes.business_back.interfaces.IEvaluacionDiabetesService;
import com.diabetes.business_back.repositories.EvaluacionDiabetesRepository;
import com.diabetes.business_back.repositories.PacienteRepository;
import com.diabetes.business_back.repositories.TipoDiabetesInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class EvaluacionDiabetesService implements IEvaluacionDiabetesService {
    @Autowired
    private EvaluacionDiabetesRepository evaluacionrepository;

    @Autowired
    private PacienteRepository pacienterepository;

    @Autowired
    private TipoDiabetesInfoRepository tipodiabetesrepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ModeloMLService modeloMLService;

    @Override
    public EvaluacionDiabetesDto grabarEvaluacion(EvaluacionDiabetesDto evaluaciondto) {
        EvaluacionDiabetes evaluacion = modelMapper.map(evaluaciondto, EvaluacionDiabetes.class);

        if (evaluaciondto.getPacientedto() != null && evaluaciondto.getPacientedto().getIdPaciente() != null) {
            Paciente paciente = pacienterepository.findById(evaluaciondto.getPacientedto().getIdPaciente())
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + evaluaciondto.getPacientedto().getIdPaciente()));
            evaluacion.setPaciente(paciente);
        }

        if (evaluacion.getFechaEvaluacion() == null) {
            evaluacion.setFechaEvaluacion(LocalDateTime.now());
        }

        EvaluacionDiabetes guardado = evaluacionrepository.save(evaluacion);
        return modelMapper.map(guardado, EvaluacionDiabetesDto.class);
    }

    @Override
    public List<EvaluacionDiabetesDto> getEvaluaciones() {
        return evaluacionrepository.findAll().stream()
                .map(evaluacion -> modelMapper.map(evaluacion, EvaluacionDiabetesDto.class))
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        if (evaluacionrepository.existsById(id)) {
            evaluacionrepository.deleteById(id);
        } else {
            throw new RuntimeException("No se encontró la evaluación con ID: " + id);
        }
    }

    @Override
    public EvaluacionDiabetesDto actualizar(EvaluacionDiabetesDto evaluaciondto) {
        Long id = evaluaciondto.getIdEvaluacion();
        if (id == null) {
            throw new RuntimeException("El ID de la evaluación no puede ser nulo");
        }

        EvaluacionDiabetes evaluacionExistente = evaluacionrepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la evaluación con ID: " + id));

        modelMapper.map(evaluaciondto, evaluacionExistente);

        if (evaluaciondto.getPacientedto() != null && evaluaciondto.getPacientedto().getIdPaciente() != null) {
            Paciente paciente = pacienterepository.findById(evaluaciondto.getPacientedto().getIdPaciente())
                    .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            evaluacionExistente.setPaciente(paciente);
        }

        EvaluacionDiabetes actualizado = evaluacionrepository.save(evaluacionExistente);
        return modelMapper.map(actualizado, EvaluacionDiabetesDto.class);
    }

    @Override
    public EvaluacionDiabetesDto obtenerPorId(Long id) {
        EvaluacionDiabetes evaluacion = evaluacionrepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluación no encontrada con ID: " + id));
        return modelMapper.map(evaluacion, EvaluacionDiabetesDto.class);
    }

    @Override
    public List<EvaluacionDiabetesDto> obtenerEvaluacionesPorPaciente(Long idPaciente) {
        return evaluacionrepository.findByPacienteIdPaciente(idPaciente).stream()
                .map(evaluacion -> modelMapper.map(evaluacion, EvaluacionDiabetesDto.class))
                .toList();
    }

    @Override
    public List<EvaluacionDiabetesDto> obtenerEvaluacionesPorTipo(String tipo) {
        return evaluacionrepository.findByTipoDiabetes(tipo).stream()
                .map(evaluacion -> modelMapper.map(evaluacion, EvaluacionDiabetesDto.class))
                .toList();
    }

    @Override
    public PrediccionResponseDto realizarPrediccion(EvaluacionRequestDto request) {
        log.info("🎯 Iniciando proceso de predicción para paciente");

        try {
            // 1. Validar datos básicos
            validarDatosPrediccion(request);
            validarDatosCompletos(request);

            // 2. Clasificar variables según rangos (para mostrar al usuario)
            Map<String, String> clasificaciones = clasificarVariables(request);

            // 3. Preparar características para el modelo ML
            Map<String, Object> features = prepararFeaturesParaML(request);

            // 4. Llamar al servicio ML para obtener predicción
            log.info("🤖 Consultando modelo ML con {} características", features.size());
            MLPredictionResponseDto mlResponse = modeloMLService.predecir(features);

            // 5. Validar respuesta del modelo
            if (mlResponse == null || mlResponse.getPredictedClass() == null) {
                throw new RuntimeException("El modelo no pudo generar una predicción válida");
            }

            // 6. Obtener información del tipo de diabetes desde BD
            TipoDiabetesInfo info = tipodiabetesrepository.findByNombreEn(mlResponse.getPredictedClass());

            // 7. Generar explicación detallada
            String explicacion = generarExplicacionCompleta(mlResponse, clasificaciones, info);

            // 8. Generar recomendaciones personalizadas
            String recomendaciones = generarRecomendacionesPersonalizadas(mlResponse, clasificaciones, info);

            // 9. Construir respuesta final
            PrediccionResponseDto response = construirRespuestaPrediccion(
                    mlResponse, clasificaciones, info, explicacion, recomendaciones
            );

            // 10. Guardar la evaluación en base de datos (opcional)
            guardarEvaluacionEnBD(request, response);

            log.info("✅ Predicción completada exitosamente: {}", response.getTipoDiabetes());
            return response;

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación en predicción: {}", e.getMessage());
            throw new RuntimeException("Error en datos de entrada: " + e.getMessage());

        } catch (Exception e) {
            log.error("❌ Error en proceso de predicción: {}", e.getMessage());
            throw new RuntimeException("Error al realizar la predicción: " + e.getMessage());
        }
    }

    private Map<String, Object> prepararFeaturesParaML(EvaluacionRequestDto request) {
        Map<String, Object> features = new HashMap<>();

        // Mapear todas las variables EXACTAMENTE como las espera tu API Python
        // Variables categóricas
        agregarCaracteristicaCategorica(features, "marcadores_geneticos", request.getMarcadoresGeneticos());
        agregarCaracteristicaCategorica(features, "autoanticuerpos", request.getAutoanticuerpos());
        agregarCaracteristicaCategorica(features, "antecedentes_familiares", request.getAntecedentesFamiliares());
        agregarCaracteristicaCategorica(features, "factores_ambientales", request.getFactoresAmbientales());
        agregarCaracteristicaCategorica(features, "etnicidad", request.getEtnicidad());
        agregarCaracteristicaCategorica(features, "habitos_alimenticios", request.getHabitosAlimenticios());
        agregarCaracteristicaCategorica(features, "prueba_tolerancia_glucosa", request.getPruebaToleranciaGlucosa());
        agregarCaracteristicaCategorica(features, "pruebas_funcion_hepatica", request.getPruebasFuncionHepatica());
        agregarCaracteristicaCategorica(features, "diagnostico_fibrosis_quistica", request.getDiagnosticoFibrosisQuistica());
        agregarCaracteristicaCategorica(features, "uso_esteroides", request.getUsoEsteroides());
        agregarCaracteristicaCategorica(features, "pruebas_geneticas", request.getPruebasGeneticas());
        agregarCaracteristicaCategorica(features, "historial_embarazos", request.getHistorialEmbarazos());
        agregarCaracteristicaCategorica(features, "diabetes_gestacional_previa", request.getDiabetesGestacionalPrevia());
        agregarCaracteristicaCategorica(features, "historial_pcos", request.getHistorialPcos());
        agregarCaracteristicaCategorica(features, "estado_tabaquismo", request.getEstadoTabaquismo());
        agregarCaracteristicaCategorica(features, "sintomas_inicio_temprano", request.getSintomasInicioTemprano());
        agregarCaracteristicaCategorica(features, "factores_socioeconomicos", request.getFactoresSocioeconomicos());
        agregarCaracteristicaCategorica(features, "consumo_alcohol", request.getConsumoAlcohol());
        agregarCaracteristicaCategorica(features, "actividad_fisica", request.getActividadFisica());
        agregarCaracteristicaCategorica(features, "prueba_orina", request.getPruebaOrina());

        // Variables numéricas
        agregarCaracteristicaNumerica(features, "niveles_insulina", request.getNivelesInsulina());
        agregarCaracteristicaNumerica(features, "edad", request.getEdad());
        agregarCaracteristicaNumerica(features, "indice_masa_corporal", request.getIndiceMasaCorporal());
        agregarCaracteristicaNumerica(features, "presion_arterial", request.getPresionArterial());
        agregarCaracteristicaNumerica(features, "niveles_colesterol", request.getNivelesColesterol());
        agregarCaracteristicaNumerica(features, "circunferencia_cintura", request.getCircunferenciaCintura());
        agregarCaracteristicaNumerica(features, "niveles_glucosa", request.getNivelesGlucosa());
        agregarCaracteristicaNumerica(features, "aumento_peso_embarazo", request.getAumentoPesoEmbarazo());
        agregarCaracteristicaNumerica(features, "salud_pancreatica", request.getSaludPancreatica());
        agregarCaracteristicaNumerica(features, "funcion_pulmonar", request.getFuncionPulmonar());
        agregarCaracteristicaNumerica(features, "evaluaciones_neurologicas", request.getEvaluacionesNeurologicas());
        agregarCaracteristicaNumerica(features, "niveles_enzimas_digestivas", request.getNivelesEnzimasDigestivas());
        agregarCaracteristicaNumerica(features, "peso_nacimiento", request.getPesoNacimiento());

        log.debug("📊 Características preparadas para ML: {}", features.size());
        return features;
    }

    private void agregarCaracteristicaCategorica(Map<String, Object> features, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            // Convertir a inglés si es necesario
            String valorConvertido = convertirAInglesSiEsNecesario(value.trim());
            features.put(key, valorConvertido);
        } else {
            features.put(key, ""); // Valor por defecto
        }
    }

    private void agregarCaracteristicaNumerica(Map<String, Object> features, String key, Object value) {
        if (value != null) {
            try {
                if (value instanceof Number) {
                    features.put(key, ((Number) value).doubleValue());
                } else if (value instanceof String) {
                    String strValue = ((String) value).trim();
                    if (!strValue.isEmpty()) {
                        features.put(key, Double.parseDouble(strValue));
                    } else {
                        features.put(key, 0.0);
                    }
                }
            } catch (NumberFormatException e) {
                features.put(key, 0.0);
            }
        } else {
            features.put(key, 0.0);
        }
    }

    private String convertirAInglesSiEsNecesario(String valor) {
        if (valor == null || valor.isEmpty()) return "";

        // Mapeo de español a inglés si es necesario
        Map<String, String> traducciones = new HashMap<>();
        traducciones.put("Sí", "Yes");
        traducciones.put("No", "No");
        traducciones.put("Positivo", "Positive");
        traducciones.put("Negativo", "Negative");
        traducciones.put("Presente", "Present");
        traducciones.put("Ausente", "Absent");
        traducciones.put("Alto", "High");
        traducciones.put("Bajo", "Low");
        traducciones.put("Moderado", "Moderate");
        traducciones.put("Saludable", "Healthy");
        traducciones.put("No saludable", "Unhealthy");
        traducciones.put("Normal", "Normal");
        traducciones.put("Anormal", "Abnormal");
        traducciones.put("Fumador", "Smoker");
        traducciones.put("No fumador", "Non-Smoker");
        traducciones.put("Complicaciones", "Complications");

        return traducciones.getOrDefault(valor, valor);
    }

    private String generarExplicacionCompleta(MLPredictionResponseDto mlResponse,
                                              Map<String, String> clasificaciones,
                                              TipoDiabetesInfo info) {
        StringBuilder explicacion = new StringBuilder();

        // 1. Resultado principal
        explicacion.append("## 📊 Resultado de la Predicción\n\n");
        explicacion.append("**Tipo de diabetes predicho:** ").append(mlResponse.getPredictedClassEs())
                .append("\n");
        explicacion.append("**Confianza del modelo:** ").append(String.format("%.1f", mlResponse.getProbability() * 100))
                .append("%\n\n");

        // 2. Factores clave que influyeron
        explicacion.append("## 🔍 Factores Clave Identificados\n\n");

        if (mlResponse.getFeatureImportance() != null && !mlResponse.getFeatureImportance().isEmpty()) {
            mlResponse.getFeatureImportance().entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> {
                        String featureName = traducirNombreFeature(entry.getKey());
                        explicacion.append("• **").append(featureName).append("**: ")
                                .append(String.format("%.0f", entry.getValue() * 100)).append("%\n");
                    });
        }

        // 3. Interpretación de valores clínicos
        explicacion.append("\n## 🩺 Interpretación de Valores\n\n");
        clasificaciones.forEach((key, value) -> {
            String nombreTraducido = traducirNombreClasificacion(key);
            explicacion.append("• **").append(nombreTraducido).append("**: ").append(value).append("\n");
        });

        // 4. Información adicional del tipo de diabetes
        if (info != null) {
            explicacion.append("\n## ℹ️ Acerca de ").append(info.getNombreEs()).append("\n\n");
            if (info.getDescripcion() != null && info.getDescripcion().length() > 200) {
                explicacion.append(info.getDescripcion().substring(0, 200)).append("...\n");
            } else if (info.getDescripcion() != null) {
                explicacion.append(info.getDescripcion()).append("\n");
            }
        }

        // 5. Otras posibles diagnósticos
        if (mlResponse.getProbabilities() != null && mlResponse.getProbabilities().size() > 1) {
            explicacion.append("\n## 🎯 Otras Posibilidades\n\n");
            mlResponse.getProbabilities().entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .skip(1)
                    .limit(3)
                    .forEach(entry -> {
                        explicacion.append("• ").append(traducirNombreDiabetes(entry.getKey()))
                                .append(": ").append(String.format("%.1f", entry.getValue() * 100))
                                .append("%\n");
                    });
        }

        return explicacion.toString();
    }

    private String generarRecomendacionesPersonalizadas(MLPredictionResponseDto mlResponse,
                                                        Map<String, String> clasificaciones,
                                                        TipoDiabetesInfo info) {
        StringBuilder recomendaciones = new StringBuilder();

        // Encabezado
        recomendaciones.append("## 📋 Recomendaciones Personalizadas\n\n");

        // 1. Recomendaciones basadas en el tipo de diabetes
        if (info != null && info.getRecomendaciones() != null && !info.getRecomendaciones().isEmpty()) {
            recomendaciones.append("**Recomendaciones específicas para ").append(info.getNombreEs()).append(":**\n");
            recomendaciones.append(info.getRecomendaciones()).append("\n\n");
        }

        // 2. Recomendaciones basadas en clasificaciones
        recomendaciones.append("**Basado en sus valores clínicos:**\n");

        // Para glucosa
        if (clasificaciones.get("glucosa") != null) {
            switch (clasificaciones.get("glucosa")) {
                case "Diabetes":
                    recomendaciones.append("• **Niveles de glucosa elevados**: Se recomienda consulta inmediata con endocrinólogo, monitoreo diario de glucosa y ajuste dietético.\n");
                    break;
                case "Prediabetes":
                    recomendaciones.append("• **Estado prediabético**: Implementar cambios en estilo de vida, realizar ejercicio regular (30 min/día) y dieta baja en carbohidratos refinados.\n");
                    break;
                case "Normal":
                    recomendaciones.append("• **Glucosa normal**: Mantener hábitos saludables y control anual.\n");
                    break;
            }
        }

        // Para insulina
        if (clasificaciones.get("insulina") != null) {
            switch (clasificaciones.get("insulina")) {
                case "Diabetes":
                    recomendaciones.append("• **Resistencia a la insulina**: Reducir consumo de azúcares simples, aumentar actividad física y considerar evaluación de síndrome metabólico.\n");
                    break;
                case "Prediabetes":
                    recomendaciones.append("• **Insulina elevada**: Aumentar consumo de fibra, realizar ejercicio de resistencia y control de peso.\n");
                    break;
            }
        }

        // Para presión arterial
        if (clasificaciones.get("presion") != null) {
            switch (clasificaciones.get("presion")) {
                case "Alta":
                    recomendaciones.append("• **Presión arterial elevada**: Reducir consumo de sal, monitoreo periódico de presión y consulta con cardiólogo.\n");
                    break;
                case "Baja":
                    recomendaciones.append("• **Presión arterial baja**: Aumentar hidratación, consumir pequeñas porciones frecuentes y evitar cambios bruscos de posición.\n");
                    break;
            }
        }

        // Para colesterol
        if (clasificaciones.get("colesterol") != null) {
            switch (clasificaciones.get("colesterol")) {
                case "Alto":
                case "Anormal":
                    recomendaciones.append("• **Colesterol elevado**: Reducir grasas saturadas, aumentar consumo de ácidos grasos omega-3 y ejercicio aeróbico regular.\n");
                    break;
            }
        }

        // 3. Recomendaciones generales
        recomendaciones.append("\n**Recomendaciones generales:**\n");
        recomendaciones.append("1. **Consulta médica**: Programar cita con especialista para confirmación diagnóstica y plan de tratamiento.\n");
        recomendaciones.append("2. **Exámenes complementarios**: Realizar hemoglobina glicosilada (HbA1c), perfil lipídico completo y función renal.\n");
        recomendaciones.append("3. **Educación diabetológica**: Participar en programas de educación sobre manejo de diabetes.\n");
        recomendaciones.append("4. **Seguimiento**: Control periódico cada 3-6 meses según indicación médica.\n");
        recomendaciones.append("5. **Emergencias**: Conocer signos de hipoglucemia/hiperglucemia y tener plan de acción.\n");

        // 4. Factores de importancia del modelo
        if (mlResponse.getFeatureImportance() != null && !mlResponse.getFeatureImportance().isEmpty()) {
            recomendaciones.append("\n**Factores críticos identificados por el modelo:**\n");
            mlResponse.getFeatureImportance().entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(3)
                    .forEach(entry -> {
                        String featureName = traducirNombreFeature(entry.getKey());
                        recomendaciones.append("• **").append(featureName).append("** fue determinante en el diagnóstico. Mantenga este valor en observación.\n");
                    });
        }

        // 5. Recomendaciones específicas según edad
        if (clasificaciones.get("edad") != null) {
            recomendaciones.append("\n**Consideraciones según grupo etario:**\n");
            switch (clasificaciones.get("edad")) {
                case "Infante":
                    recomendaciones.append("• **Niños**: Monitoreo estrecho por pediatra endocrinólogo, atención especial a crecimiento y desarrollo.\n");
                    break;
                case "Adolescente":
                    recomendaciones.append("• **Adolescentes**: Educación sobre autocuidado, apoyo psicológico y adaptación escolar.\n");
                    break;
                case "Adulto Mayor":
                    recomendaciones.append("• **Adulto mayor**: Evaluación de medicamentos concurrentes, prevención de complicaciones y soporte familiar.\n");
                    break;
            }
        }

        return recomendaciones.toString();
    }

    private String traducirNombreFeature(String featureName) {
        Map<String, String> traducciones = new HashMap<>();
        traducciones.put("niveles_glucosa", "Niveles de Glucosa");
        traducciones.put("niveles_insulina", "Niveles de Insulina");
        traducciones.put("edad", "Edad");
        traducciones.put("indice_masa_corporal", "Índice de Masa Corporal");
        traducciones.put("autoanticuerpos", "Autoanticuerpos");
        traducciones.put("antecedentes_familiares", "Antecedentes Familiares");
        traducciones.put("presion_arterial", "Presión Arterial");
        traducciones.put("niveles_colesterol", "Niveles de Colesterol");
        traducciones.put("circunferencia_cintura", "Circunferencia de Cintura");
        traducciones.put("aumento_peso_embarazo", "Aumento de Peso en Embarazo");
        traducciones.put("salud_pancreatica", "Salud Pancreática");
        traducciones.put("funcion_pulmonar", "Función Pulmonar");
        traducciones.put("evaluaciones_neurologicas", "Evaluaciones Neurológicas");
        traducciones.put("niveles_enzimas_digestivas", "Niveles de Enzimas Digestivas");
        traducciones.put("peso_nacimiento", "Peso al Nacer");

        return traducciones.getOrDefault(featureName, featureName);
    }

    private String traducirNombreClasificacion(String key) {
        Map<String, String> traducciones = new HashMap<>();
        traducciones.put("presion", "Presión Arterial");
        traducciones.put("colesterol", "Colesterol");
        traducciones.put("insulina", "Insulina");
        traducciones.put("glucosa", "Glucosa");
        traducciones.put("edad", "Grupo de Edad");
        traducciones.put("enzimas", "Enzimas Digestivas");
        return traducciones.getOrDefault(key, key);
    }

    private String traducirNombreDiabetes(String diabetesName) {
        MLPredictionResponseDto temp = new MLPredictionResponseDto();
        temp.setPredictedClass(diabetesName);
        return temp.getPredictedClassEs();
    }

    private PrediccionResponseDto construirRespuestaPrediccion(
            MLPredictionResponseDto mlResponse,
            Map<String, String> clasificaciones,
            TipoDiabetesInfo info,
            String explicacion,
            String recomendaciones) {

        PrediccionResponseDto response = new PrediccionResponseDto();

        response.setTipoDiabetes(mlResponse.getPredictedClass());
        response.setTipoDiabetesEs(mlResponse.getPredictedClassEs());
        response.setProbabilidad(mlResponse.getProbability());
        response.setExplicacion(explicacion);
        response.setClasificaciones(clasificaciones);

        if (info != null) {
            TipoDiabetesInfoDto infoDto = new TipoDiabetesInfoDto();
            infoDto.setIdTipoDiabetes(info.getIdTipoDiabetes());
            infoDto.setNombreEn(info.getNombreEn());
            infoDto.setNombreEs(info.getNombreEs());
            infoDto.setDescripcion(info.getDescripcion());
            infoDto.setCausas(info.getCausas());
            infoDto.setSintomas(info.getSintomas());
            infoDto.setTratamiento(info.getTratamiento());
            infoDto.setRecomendaciones(info.getRecomendaciones());
            infoDto.setEsComun(info.getEsComun());
            response.setInformacionTipo(infoDto);
        }

        response.setRecomendacionesPersonalizadas(recomendaciones);
        response.setFechaPrediccion(LocalDateTime.now());

        return response;
    }

    private void guardarEvaluacionEnBD(EvaluacionRequestDto request, PrediccionResponseDto response) {
        try {
            EvaluacionDiabetes evaluacion = new EvaluacionDiabetes();

            // Mapear datos básicos
            evaluacion.setEdad(request.getEdad());
            evaluacion.setNivelesGlucosa(request.getNivelesGlucosa());
            evaluacion.setNivelesInsulina(request.getNivelesInsulina());
            evaluacion.setIndiceMasaCorporal(request.getIndiceMasaCorporal());
            evaluacion.setPresionArterial(request.getPresionArterial());
            evaluacion.setNivelesColesterol(request.getNivelesColesterol());
            evaluacion.setCircunferenciaCintura(request.getCircunferenciaCintura());

            // Mapear variables categóricas
            evaluacion.setMarcadoresGeneticos(request.getMarcadoresGeneticos());
            evaluacion.setAutoanticuerpos(request.getAutoanticuerpos());
            evaluacion.setAntecedentesFamiliares(request.getAntecedentesFamiliares());
            evaluacion.setFactoresAmbientales(request.getFactoresAmbientales());
            evaluacion.setEtnicidad(request.getEtnicidad());
            evaluacion.setHabitosAlimenticios(request.getHabitosAlimenticios());
            evaluacion.setPruebaToleranciaGlucosa(request.getPruebaToleranciaGlucosa());
            evaluacion.setPruebasFuncionHepatica(request.getPruebasFuncionHepatica());
            evaluacion.setDiagnosticoFibrosisQuistica(request.getDiagnosticoFibrosisQuistica());
            evaluacion.setUsoEsteroides(request.getUsoEsteroides());
            evaluacion.setPruebasGeneticas(request.getPruebasGeneticas());
            evaluacion.setHistorialEmbarazos(request.getHistorialEmbarazos());
            evaluacion.setDiabetesGestacionalPrevia(request.getDiabetesGestacionalPrevia());
            evaluacion.setHistorialPcos(request.getHistorialPcos());
            evaluacion.setEstadoTabaquismo(request.getEstadoTabaquismo());
            evaluacion.setSintomasInicioTemprano(request.getSintomasInicioTemprano());
            evaluacion.setFactoresSocioeconomicos(request.getFactoresSocioeconomicos());
            evaluacion.setConsumoAlcohol(request.getConsumoAlcohol());
            evaluacion.setActividadFisica(request.getActividadFisica());
            evaluacion.setPruebaOrina(request.getPruebaOrina());

            // Mapear datos de la respuesta
            evaluacion.setTipoDiabetesPredicho(response.getTipoDiabetes());
            evaluacion.setProbabilidad(response.getProbabilidad());
            evaluacion.setExplicacion(response.getExplicacion());
            evaluacion.setRecomendaciones(response.getRecomendacionesPersonalizadas());
            evaluacion.setFechaEvaluacion(response.getFechaPrediccion());

            // Mapear clasificaciones
            if (response.getClasificaciones() != null) {
                evaluacion.setClasificacionPresion(response.getClasificaciones().get("presion"));
                evaluacion.setClasificacionColesterol(response.getClasificaciones().get("colesterol"));
                evaluacion.setClasificacionInsulina(response.getClasificaciones().get("insulina"));
                evaluacion.setClasificacionGlucosa(response.getClasificaciones().get("glucosa"));
                evaluacion.setClasificacionEdad(response.getClasificaciones().get("edad"));
            }

            // Guardar si hay paciente asociado
            if (request.getPacientedto() != null && request.getPacientedto().getIdPaciente() != null) {
                Paciente paciente = pacienterepository.findById(request.getPacientedto().getIdPaciente())
                        .orElse(null);
                evaluacion.setPaciente(paciente);
            }

            evaluacionrepository.save(evaluacion);
            log.info("✅ Evaluación guardada en BD con ID: {}", evaluacion.getIdEvaluacion());

        } catch (Exception e) {
            log.error("❌ Error al guardar evaluación en BD: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Long> obtenerEstadisticas() {
        Map<String, Long> estadisticas = new HashMap<>();

        // TODOS los 12 tipos de diabetes que tu modelo puede predecir
        List<String> tipos = Arrays.asList(
                "Steroid-Induced Diabetes",
                "Prediabetic",
                "Type 1 Diabetes",
                "Wolfram Syndrome",
                "LADA",
                "Type 2 Diabetes",
                "Wolcott-Rallison Syndrome",
                "Secondary Diabetes",
                "Type 3c Diabetes (Pancreatogenic Diabetes)",
                "Gestational Diabetes",
                "Cystic Fibrosis-Related Diabetes (CFRD)",
                "MODY"
        );

        for (String tipo : tipos) {
            try {
                Long count = evaluacionrepository.countByTipoDiabetes(tipo);
                estadisticas.put(tipo, count != null ? count : 0L);
            } catch (Exception e) {
                log.warn("Error contando evaluaciones para tipo {}: {}", tipo, e.getMessage());
                estadisticas.put(tipo, 0L);
            }
        }

        long totalEvaluaciones = evaluacionrepository.count();
        estadisticas.put("total", totalEvaluaciones);

        return estadisticas;
    }

    @Override
    public Map<String, Object> obtenerEstadisticasCompletas() {
        Map<String, Object> estadisticasCompletas = new HashMap<>();

        // Obtener conteos básicos
        Map<String, Long> conteos = obtenerEstadisticas();
        estadisticasCompletas.put("conteos", conteos);

        long total = conteos.get("total");

        // Calcular porcentajes si hay datos
        if (total > 0) {
            Map<String, String> porcentajes = new HashMap<>();
            List<String> tipos = Arrays.asList(
                    "Steroid-Induced Diabetes",
                    "Prediabetic",
                    "Type 1 Diabetes",
                    "Wolfram Syndrome",
                    "LADA",
                    "Type 2 Diabetes",
                    "Wolcott-Rallison Syndrome",
                    "Secondary Diabetes",
                    "Type 3c Diabetes (Pancreatogenic Diabetes)",
                    "Gestational Diabetes",
                    "Cystic Fibrosis-Related Diabetes (CFRD)",
                    "MODY"
            );

            for (String tipo : tipos) {
                long count = conteos.get(tipo);
                double porcentaje = (count * 100.0) / total;
                porcentajes.put(tipo, String.format("%.1f%%", porcentaje));
            }

            estadisticasCompletas.put("porcentajes", porcentajes);

            // Encontrar el tipo más común
            String tipoMasComun = tipos.stream()
                    .max((t1, t2) -> Long.compare(conteos.get(t1), conteos.get(t2)))
                    .orElse("Sin datos");

            estadisticasCompletas.put("tipo_mas_comun", tipoMasComun);
            estadisticasCompletas.put("conteo_mas_comun", conteos.get(tipoMasComun));

            // Calcular distribución
            long comunes = conteos.get("Type 1 Diabetes") + conteos.get("Type 2 Diabetes") +
                    conteos.get("Prediabetic") + conteos.get("Gestational Diabetes");
            long raros = total - comunes;

            estadisticasCompletas.put("tipos_comunes_total", comunes);
            estadisticasCompletas.put("tipos_raros_total", raros);
            estadisticasCompletas.put("porcentaje_comunes", String.format("%.1f%%", (comunes * 100.0) / total));
            estadisticasCompletas.put("porcentaje_raros", String.format("%.1f%%", (raros * 100.0) / total));
        }

        estadisticasCompletas.put("fecha_consulta", LocalDateTime.now());

        return estadisticasCompletas;
    }

    private void validarDatosPrediccion(EvaluacionRequestDto request) {
        if (request.getEdad() == null || request.getEdad() < 0 || request.getEdad() > 120) {
            throw new RuntimeException("Edad inválida. Debe estar entre 0 y 120 años");
        }

        if (request.getNivelesGlucosa() == null || request.getNivelesGlucosa() < 0) {
            throw new RuntimeException("Niveles de glucosa inválidos");
        }
    }

    private void validarDatosCompletos(EvaluacionRequestDto request) {
        List<String> errores = new ArrayList<>();

        // Validar campos obligatorios
        if (request.getEdad() == null || request.getEdad() < 0 || request.getEdad() > 120) {
            errores.add("Edad inválida. Debe estar entre 0 y 120 años");
        }

        if (request.getNivelesGlucosa() == null || request.getNivelesGlucosa() < 0 || request.getNivelesGlucosa() > 1000) {
            errores.add("Niveles de glucosa inválidos. Rango: 0-1000 mg/dL");
        }

        if (request.getNivelesInsulina() == null || request.getNivelesInsulina() < 0 || request.getNivelesInsulina() > 500) {
            errores.add("Niveles de insulina inválidos. Rango: 0-500 μU/mL");
        }

        if (request.getIndiceMasaCorporal() == null || request.getIndiceMasaCorporal() < 10 || request.getIndiceMasaCorporal() > 60) {
            errores.add("Índice de masa corporal inválido. Rango: 10-60 kg/m²");
        }

        if (request.getPresionArterial() == null || request.getPresionArterial() < 60 || request.getPresionArterial() > 250) {
            errores.add("Presión arterial inválida. Rango: 60-250 mmHg");
        }

        // Validar campos categóricos básicos
        if (request.getAutoanticuerpos() == null || request.getAutoanticuerpos().trim().isEmpty()) {
            errores.add("Campo 'autoanticuerpos' es requerido");
        }

        if (request.getAntecedentesFamiliares() == null || request.getAntecedentesFamiliares().trim().isEmpty()) {
            errores.add("Campo 'antecedentesFamiliares' es requerido");
        }

        if (request.getMarcadoresGeneticos() == null || request.getMarcadoresGeneticos().trim().isEmpty()) {
            errores.add("Campo 'marcadoresGeneticos' es requerido");
        }

        if (!errores.isEmpty()) {
            throw new IllegalArgumentException("Errores de validación: " + String.join(", ", errores));
        }
    }

    private Map<String, String> clasificarVariables(EvaluacionRequestDto request) {
        Map<String, String> clasificaciones = new HashMap<>();

        // Clasificar presión arterial
        if (request.getPresionArterial() != null) {
            if (request.getPresionArterial() < 90) {
                clasificaciones.put("presion", "Baja");
            } else if (request.getPresionArterial() <= 130) {
                clasificaciones.put("presion", "Normal");
            } else {
                clasificaciones.put("presion", "Alta");
            }
        }

        // Clasificar colesterol
        if (request.getNivelesColesterol() != null) {
            if (request.getNivelesColesterol() < 200) {
                clasificaciones.put("colesterol", "Normal");
            } else if (request.getNivelesColesterol() <= 239) {
                clasificaciones.put("colesterol", "Alto");
            } else {
                clasificaciones.put("colesterol", "Anormal");
            }
        }

        // Clasificar insulina
        if (request.getNivelesInsulina() != null) {
            if (request.getNivelesInsulina() <= 25) {
                clasificaciones.put("insulina", "Normal");
            } else if (request.getNivelesInsulina() <= 40) {
                clasificaciones.put("insulina", "Prediabetes");
            } else {
                clasificaciones.put("insulina", "Diabetes");
            }
        }

        // Clasificar glucosa
        if (request.getNivelesGlucosa() != null) {
            if (request.getNivelesGlucosa() < 100) {
                clasificaciones.put("glucosa", "Normal");
            } else if (request.getNivelesGlucosa() <= 125) {
                clasificaciones.put("glucosa", "Prediabetes");
            } else {
                clasificaciones.put("glucosa", "Diabetes");
            }
        }

        // Clasificar edad
        if (request.getEdad() != null) {
            if (request.getEdad() <= 12) {
                clasificaciones.put("edad", "Infante");
            } else if (request.getEdad() <= 25) {
                clasificaciones.put("edad", "Adolescente");
            } else if (request.getEdad() <= 60) {
                clasificaciones.put("edad", "Adulto");
            } else {
                clasificaciones.put("edad", "Adulto Mayor");
            }
        }

        return clasificaciones;
    }
}
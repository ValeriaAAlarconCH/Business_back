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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        // 1. Validar datos
        validarDatosPrediccion(request);

        // 2. Clasificar variables según rangos
        Map<String, String> clasificaciones = clasificarVariables(request);

        // 3. Preparar características para el modelo ML
        Map<String, Object> features = prepararFeaturesParaML(request);

        // 4. Crear request para el modelo ML
        MLPredictionRequestDto mlRequest = new MLPredictionRequestDto();
        mlRequest.setFeatures(features);

        // 5. Llamar al modelo ML REAL
        MLPredictionResponseDto mlResponse = modeloMLService.predecir(mlRequest);

        // 6. Obtener información del tipo de diabetes
        TipoDiabetesInfo info = tipodiabetesrepository.findByNombreEn(mlResponse.getPredictedClass());

        // 7. Generar explicación basada en el modelo real
        String explicacion = generarExplicacionML(mlResponse, clasificaciones);

        // 8. Generar recomendaciones
        String recomendaciones = generarRecomendaciones(mlResponse.getPredictedClass(), clasificaciones);

        // 9. Construir respuesta
        PrediccionResponseDto response = new PrediccionResponseDto();
        response.setTipoDiabetes(mlResponse.getPredictedClass());
        response.setTipoDiabetesEs(info != null ? info.getNombreEs() : mlResponse.getPredictedClass());
        response.setProbabilidad(mlResponse.getProbability());
        response.setExplicacion(explicacion);
        response.setClasificaciones(clasificaciones);
        response.setInformacionTipo(info != null ? modelMapper.map(info, TipoDiabetesInfoDto.class) : null);
        response.setRecomendacionesPersonalizadas(recomendaciones);
        response.setFechaPrediccion(LocalDateTime.now());

        // 10. Guardar la evaluación en base de datos (opcional)
        guardarEvaluacionEnBD(request, response);

        return response;
    }

    private Map<String, Object> prepararFeaturesParaML(EvaluacionRequestDto request) {
        Map<String, Object> features = new HashMap<>();

        // Mapear todas las variables del request al formato que espera el modelo
        features.put("marcadores_geneticos", request.getMarcadoresGeneticos());
        features.put("autoanticuerpos", request.getAutoanticuerpos());
        features.put("antecedentes_familiares", request.getAntecedentesFamiliares());
        features.put("factores_ambientales", request.getFactoresAmbientales());
        features.put("etnicidad", request.getEtnicidad());
        features.put("habitos_alimenticios", request.getHabitosAlimenticios());
        features.put("prueba_tolerancia_glucosa", request.getPruebaToleranciaGlucosa());
        features.put("pruebas_funcion_hepatica", request.getPruebasFuncionHepatica());
        features.put("diagnostico_fibrosis_quistica", request.getDiagnosticoFibrosisQuistica());
        features.put("uso_esteroides", request.getUsoEsteroides());
        features.put("pruebas_geneticas", request.getPruebasGeneticas());
        features.put("historial_embarazos", request.getHistorialEmbarazos());
        features.put("diabetes_gestacional_previa", request.getDiabetesGestacionalPrevia());
        features.put("historial_pcos", request.getHistorialPcos());
        features.put("estado_tabaquismo", request.getEstadoTabaquismo());
        features.put("sintomas_inicio_temprano", request.getSintomasInicioTemprano());
        features.put("factores_socioeconomicos", request.getFactoresSocioeconomicos());
        features.put("consumo_alcohol", request.getConsumoAlcohol());
        features.put("actividad_fisica", request.getActividadFisica());
        features.put("prueba_orina", request.getPruebaOrina());

        features.put("niveles_insulina", request.getNivelesInsulina());
        features.put("edad", request.getEdad());
        features.put("indice_masa_corporal", request.getIndiceMasaCorporal());
        features.put("presion_arterial", request.getPresionArterial());
        features.put("niveles_colesterol", request.getNivelesColesterol());
        features.put("circunferencia_cintura", request.getCircunferenciaCintura());
        features.put("niveles_glucosa", request.getNivelesGlucosa());
        features.put("aumento_peso_embarazo", request.getAumentoPesoEmbarazo());
        features.put("salud_pancreatica", request.getSaludPancreatica());
        features.put("funcion_pulmonar", request.getFuncionPulmonar());
        features.put("evaluaciones_neurologicas", request.getEvaluacionesNeurologicas());
        features.put("niveles_enzimas_digestivas", request.getNivelesEnzimasDigestivas());
        features.put("peso_nacimiento", request.getPesoNacimiento());

        return features;
    }

    private String generarExplicacionML(MLPredictionResponseDto mlResponse, Map<String, String> clasificaciones) {
        StringBuilder explicacion = new StringBuilder();

        explicacion.append("Predicción del modelo ML: ").append(mlResponse.getPredictedClass())
                .append("\nConfianza: ").append(String.format("%.1f", mlResponse.getProbability() * 100)).append("%\n\n");

        if (mlResponse.getFeatureImportance() != null && !mlResponse.getFeatureImportance().isEmpty()) {
            explicacion.append("Variables más influyentes en la predicción:\n");
            mlResponse.getFeatureImportance().entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> {
                        explicacion.append("• ").append(entry.getKey()).append(": ")
                                .append(String.format("%.0f", entry.getValue() * 100)).append("%\n");
                    });
        }

        // Agregar clasificaciones interpretadas
        explicacion.append("\nInterpretación de valores:\n");
        clasificaciones.forEach((key, value) -> {
            explicacion.append("• ").append(key).append(": ").append(value).append("\n");
        });

        return explicacion.toString();
    }

    private void guardarEvaluacionEnBD(EvaluacionRequestDto request, PrediccionResponseDto response) {
        try {
            EvaluacionDiabetes evaluacion = new EvaluacionDiabetes();

            // Mapear datos del request
            modelMapper.map(request, evaluacion);

            // Mapear datos de la respuesta
            evaluacion.setTipoDiabetesPredicho(response.getTipoDiabetes());
            evaluacion.setProbabilidad(response.getProbabilidad());
            evaluacion.setExplicacion(response.getExplicacion());
            evaluacion.setRecomendaciones(response.getRecomendacionesPersonalizadas());
            evaluacion.setFechaEvaluacion(response.getFechaPrediccion());

            // Mapear clasificaciones
            evaluacion.setClasificacionPresion(response.getClasificaciones().get("presion"));
            evaluacion.setClasificacionColesterol(response.getClasificaciones().get("colesterol"));
            evaluacion.setClasificacionInsulina(response.getClasificaciones().get("insulina"));
            evaluacion.setClasificacionGlucosa(response.getClasificaciones().get("glucosa"));
            evaluacion.setClasificacionEdad(response.getClasificaciones().get("edad"));

            // Guardar si hay paciente asociado
            if (request.getPacientedto() != null && request.getPacientedto().getIdPaciente() != null) {
                Paciente paciente = pacienterepository.findById(request.getPacientedto().getIdPaciente())
                        .orElse(null);
                evaluacion.setPaciente(paciente);
            }

            evaluacionrepository.save(evaluacion);
            log.info("Evaluación guardada en BD con ID: {}", evaluacion.getIdEvaluacion());

        } catch (Exception e) {
            log.error("Error al guardar evaluación en BD: {}", e.getMessage());
        }
    }

    @Override
    public Map<String, Long> obtenerEstadisticas() {
        Map<String, Long> estadisticas = new HashMap<>();

        List<String> tipos = List.of("Type 1 Diabetes", "Type 2 Diabetes", "Prediabetic", "Gestational Diabetes");

        for (String tipo : tipos) {
            Long count = evaluacionrepository.countByTipoDiabetes(tipo);
            estadisticas.put(tipo, count);
        }

        estadisticas.put("total", evaluacionrepository.count());

        return estadisticas;
    }

    private void validarDatosPrediccion(EvaluacionRequestDto request) {
        if (request.getEdad() == null || request.getEdad() < 0 || request.getEdad() > 120) {
            throw new RuntimeException("Edad inválida. Debe estar entre 0 y 120 años");
        }

        if (request.getNivelesGlucosa() == null || request.getNivelesGlucosa() < 0) {
            throw new RuntimeException("Niveles de glucosa inválidos");
        }

    }

    private Map<String, String> clasificarVariables(EvaluacionRequestDto request) {
        Map<String, String> clasificaciones = new HashMap<>();

        // presión arterial
        if (request.getPresionArterial() < 90) {
            clasificaciones.put("presion", "Baja");
        } else if (request.getPresionArterial() <= 130) {
            clasificaciones.put("presion", "Normal");
        } else {
            clasificaciones.put("presion", "Alta");
        }

        // colesterol
        if (request.getNivelesColesterol() < 200) {
            clasificaciones.put("colesterol", "Normal");
        } else if (request.getNivelesColesterol() <= 239) {
            clasificaciones.put("colesterol", "Alto");
        } else {
            clasificaciones.put("colesterol", "Anormal");
        }

        // insulina
        if (request.getNivelesInsulina() <= 25) {
            clasificaciones.put("insulina", "Normal");
        } else if (request.getNivelesInsulina() <= 40) {
            clasificaciones.put("insulina", "Prediabetes");
        } else {
            clasificaciones.put("insulina", "Diabetes");
        }

        // glucosa
        if (request.getNivelesGlucosa() < 100) {
            clasificaciones.put("glucosa", "Normal");
        } else if (request.getNivelesGlucosa() <= 125) {
            clasificaciones.put("glucosa", "Prediabetes");
        } else {
            clasificaciones.put("glucosa", "Diabetes");
        }

        // edad
        if (request.getEdad() <= 12) {
            clasificaciones.put("edad", "Infante");
        } else if (request.getEdad() <= 25) {
            clasificaciones.put("edad", "Adolescente");
        } else if (request.getEdad() <= 60) {
            clasificaciones.put("edad", "Adulto");
        } else {
            clasificaciones.put("edad", "Adulto Mayor");
        }

        return clasificaciones;
    }

    private String simularPrediccionML(EvaluacionRequestDto request) {
        // Esta es una simulación - aquí integrarías tu modelo real
        // Por ejemplo, podrías llamar a una API de Python que tenga tu modelo

        // Lógica simple basada en glucosa e insulina
        if (request.getNivelesGlucosa() > 125 && request.getNivelesInsulina() > 40) {
            return "Type 2 Diabetes";
        } else if (request.getNivelesGlucosa() > 125 && request.getNivelesInsulina() < 10) {
            return "Type 1 Diabetes";
        } else if (request.getNivelesGlucosa() >= 100 && request.getNivelesGlucosa() <= 125) {
            return "Prediabetic";
        } else {
            return "Type 2 Diabetes"; // Default
        }
    }

    private String generarExplicacion(String tipoDiabetes, Map<String, String> clasificaciones, Double probabilidad) {
        StringBuilder explicacion = new StringBuilder();
        explicacion.append("Predicción: ").append(tipoDiabetes)
                .append(" con una probabilidad del ").append(String.format("%.1f", probabilidad * 100)).append("%\n\n");

        explicacion.append("Factores clave identificados:\n");

        if (clasificaciones.get("glucosa").equals("Diabetes")) {
            explicacion.append("• Niveles de glucosa elevados (diabéticos)\n");
        }
        if (clasificaciones.get("insulina").equals("Diabetes")) {
            explicacion.append("• Resistencia a la insulina detectada\n");
        }
        if (clasificaciones.get("presion").equals("Alta")) {
            explicacion.append("• Presión arterial alta\n");
        }
        if (clasificaciones.get("colesterol").equals("Alto") || clasificaciones.get("colesterol").equals("Anormal")) {
            explicacion.append("• Colesterol elevado\n");
        }

        return explicacion.toString();
    }

    private String generarRecomendaciones(String tipoDiabetes, Map<String, String> clasificaciones) {
        StringBuilder recomendaciones = new StringBuilder();

        recomendaciones.append("Recomendaciones generales:\n");
        recomendaciones.append("1. Consultar con un médico especialista para confirmar diagnóstico\n");
        recomendaciones.append("2. Realizar exámenes de laboratorio complementarios\n");
        recomendaciones.append("3. Seguir un plan de alimentación saludable\n");

        if (clasificaciones.get("glucosa").equals("Diabetes") || clasificaciones.get("glucosa").equals("Prediabetes")) {
            recomendaciones.append("4. Monitorear niveles de glucosa regularmente\n");
        }
        if (clasificaciones.get("presion").equals("Alta")) {
            recomendaciones.append("5. Controlar la presión arterial con seguimiento médico\n");
        }

        return recomendaciones.toString();
    }
}

package com.diabetes.business_back.interfaces;

import com.diabetes.business_back.dtos.EvaluacionDiabetesDto;
import com.diabetes.business_back.dtos.EvaluacionRequestDto;
import com.diabetes.business_back.dtos.PrediccionResponseDto;

import java.util.List;
import java.util.Map;

public interface IEvaluacionDiabetesService {
    public EvaluacionDiabetesDto grabarEvaluacion(EvaluacionDiabetesDto evaluaciondto);
    public List<EvaluacionDiabetesDto> getEvaluaciones();
    void eliminar(Long id);
    EvaluacionDiabetesDto actualizar(EvaluacionDiabetesDto evaluaciondto);
    public EvaluacionDiabetesDto obtenerPorId(Long id);
    List<EvaluacionDiabetesDto> obtenerEvaluacionesPorPaciente(Long idPaciente);
    List<EvaluacionDiabetesDto> obtenerEvaluacionesPorTipo(String tipo);
    PrediccionResponseDto realizarPrediccion(EvaluacionRequestDto request);
    Map<String, Long> obtenerEstadisticas();
}

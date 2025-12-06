package com.diabetes.business_back.repositories;

import com.diabetes.business_back.entities.EvaluacionDiabetes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluacionDiabetesRepository extends JpaRepository<EvaluacionDiabetes, Long> {
    List<EvaluacionDiabetes> findByPacienteIdPaciente(Long idPaciente);

    @Query("SELECT e FROM com.diabetes.business_back.entities.EvaluacionDiabetes e WHERE e.tipoDiabetesPredicho = :tipo ORDER BY e.fechaEvaluacion DESC")
    List<EvaluacionDiabetes> findByTipoDiabetes(@Param("tipo") String tipo);

    @Query("SELECT COUNT(e) FROM com.diabetes.business_back.entities.EvaluacionDiabetes e WHERE e.tipoDiabetesPredicho = :tipo")
    Long countByTipoDiabetes(@Param("tipo") String tipo);
}

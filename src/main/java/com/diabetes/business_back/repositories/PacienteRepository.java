package com.diabetes.business_back.repositories;

import com.diabetes.business_back.entities.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Paciente findByCodigoPaciente(String codigoPaciente);
    Paciente findByEmail(String email);
}

package com.diabetes.business_back.interfaces;

import com.diabetes.business_back.dtos.PacienteDto;

import java.util.List;

public interface IPacienteService {
    public PacienteDto grabarPaciente(PacienteDto pacientedto);
    public List<PacienteDto> getPacientes();
    void eliminar(Long id);
    PacienteDto actualizar(PacienteDto pacientedto);
    public PacienteDto obtenerPorId(Long id);
    PacienteDto obtenerPorCodigo(String codigo);
    PacienteDto obtenerPorEmail(String email);
}
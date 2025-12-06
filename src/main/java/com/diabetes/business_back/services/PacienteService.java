package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.PacienteDto;
import com.diabetes.business_back.entities.Paciente;
import com.diabetes.business_back.interfaces.IPacienteService;
import com.diabetes.business_back.repositories.PacienteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService implements IPacienteService {
    @Autowired
    private PacienteRepository pacienterepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public PacienteDto grabarPaciente(PacienteDto pacientedto) {
        Paciente paciente = modelMapper.map(pacientedto, Paciente.class);
        Paciente guardar = pacienterepository.save(paciente);
        return modelMapper.map(guardar, PacienteDto.class);
    }

    @Override
    public List<PacienteDto> getPacientes() {
        return pacienterepository.findAll().stream()
                .map(paciente -> modelMapper.map(paciente, PacienteDto.class))
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        if (pacienterepository.existsById(id)) {
            pacienterepository.deleteById(id);
        } else {
            throw new RuntimeException("No se encontró el paciente con ID: " + id);
        }
    }

    @Override
    public PacienteDto actualizar(PacienteDto pacientedto) {
        Long id = pacientedto.getIdPaciente();
        if (id == null) {
            throw new RuntimeException("El ID del paciente no puede ser nulo");
        }

        Paciente pacienteExistente = pacienterepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el paciente con ID: " + id));

        pacienteExistente.setNombre(pacientedto.getNombre());
        pacienteExistente.setFechaNacimiento(pacientedto.getFechaNacimiento());
        pacienteExistente.setGenero(pacientedto.getGenero());
        pacienteExistente.setTelefono(pacientedto.getTelefono());
        pacienteExistente.setEmail(pacientedto.getEmail());
        pacienteExistente.setDireccion(pacientedto.getDireccion());

        Paciente actualizado = pacienterepository.save(pacienteExistente);
        return modelMapper.map(actualizado, PacienteDto.class);
    }

    @Override
    public PacienteDto obtenerPorId(Long id) {
        Paciente paciente = pacienterepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
        return modelMapper.map(paciente, PacienteDto.class);
    }

    @Override
    public PacienteDto obtenerPorCodigo(String codigo) {
        Paciente paciente = pacienterepository.findByCodigoPaciente(codigo);
        if (paciente == null) {
            throw new RuntimeException("Paciente no encontrado con código: " + codigo);
        }
        return modelMapper.map(paciente, PacienteDto.class);
    }

    @Override
    public PacienteDto obtenerPorEmail(String email) {
        Paciente paciente = pacienterepository.findByEmail(email);
        if (paciente == null) {
            throw new RuntimeException("Paciente no encontrado con email: " + email);
        }
        return modelMapper.map(paciente, PacienteDto.class);
    }
}

package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.TipoDiabetesInfoDto;
import com.diabetes.business_back.entities.TipoDiabetesInfo;
import com.diabetes.business_back.interfaces.ITipoDiabetesInfoService;
import com.diabetes.business_back.repositories.TipoDiabetesInfoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoDiabetesInfoService implements ITipoDiabetesInfoService {
    @Autowired
    private TipoDiabetesInfoRepository tipodiabetesrepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public TipoDiabetesInfoDto grabarTipoDiabetes(TipoDiabetesInfoDto tipodto) {
        TipoDiabetesInfo tipo = modelMapper.map(tipodto, TipoDiabetesInfo.class);
        TipoDiabetesInfo guardar = tipodiabetesrepository.save(tipo);
        return modelMapper.map(guardar, TipoDiabetesInfoDto.class);
    }

    @Override
    public List<TipoDiabetesInfoDto> getTiposDiabetes() {
        return tipodiabetesrepository.findAll().stream()
                .map(tipo -> modelMapper.map(tipo, TipoDiabetesInfoDto.class))
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        if (tipodiabetesrepository.existsById(id)) {
            tipodiabetesrepository.deleteById(id);
        } else {
            throw new RuntimeException("No se encontró el tipo de diabetes con ID: " + id);
        }
    }

    @Override
    public TipoDiabetesInfoDto actualizar(TipoDiabetesInfoDto tipodto) {
        Long id = tipodto.getIdTipoDiabetes();
        if (id == null) {
            throw new RuntimeException("El ID del tipo de diabetes no puede ser nulo");
        }

        TipoDiabetesInfo tipoExistente = tipodiabetesrepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el tipo de diabetes con ID: " + id));

        tipoExistente.setNombreEn(tipodto.getNombreEn());
        tipoExistente.setNombreEs(tipodto.getNombreEs());
        tipoExistente.setDescripcion(tipodto.getDescripcion());
        tipoExistente.setCausas(tipodto.getCausas());
        tipoExistente.setSintomas(tipodto.getSintomas());
        tipoExistente.setTratamiento(tipodto.getTratamiento());
        tipoExistente.setRecomendaciones(tipodto.getRecomendaciones());
        tipoExistente.setEsComun(tipodto.getEsComun());

        TipoDiabetesInfo actualizado = tipodiabetesrepository.save(tipoExistente);
        return modelMapper.map(actualizado, TipoDiabetesInfoDto.class);
    }

    @Override
    public TipoDiabetesInfoDto obtenerPorId(Long id) {
        TipoDiabetesInfo tipo = tipodiabetesrepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de diabetes no encontrado con ID: " + id));
        return modelMapper.map(tipo, TipoDiabetesInfoDto.class);
    }

    @Override
    public TipoDiabetesInfoDto obtenerPorNombreEn(String nombreEn) {
        TipoDiabetesInfo tipo = tipodiabetesrepository.findByNombreEn(nombreEn);
        if (tipo == null) {
            throw new RuntimeException("Tipo de diabetes no encontrado con nombre: " + nombreEn);
        }
        return modelMapper.map(tipo, TipoDiabetesInfoDto.class);
    }

    @Override
    public List<TipoDiabetesInfoDto> obtenerTiposComunes() {
        return tipodiabetesrepository.findByEsComun(true).stream()
                .map(tipo -> modelMapper.map(tipo, TipoDiabetesInfoDto.class))
                .toList();
    }
}

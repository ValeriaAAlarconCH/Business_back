package com.diabetes.business_back.services;

import com.diabetes.business_back.dtos.GuiaCampoDto;
import com.diabetes.business_back.entities.GuiaCampo;
import com.diabetes.business_back.interfaces.IGuiaCampoService;
import com.diabetes.business_back.repositories.GuiaCampoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuiaCampoService implements IGuiaCampoService {
    @Autowired
    private GuiaCampoRepository guiacamporepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public GuiaCampoDto grabarGuiaCampo(GuiaCampoDto guiadto) {
        GuiaCampo guia = modelMapper.map(guiadto, GuiaCampo.class);
        GuiaCampo guardar = guiacamporepository.save(guia);
        return modelMapper.map(guardar, GuiaCampoDto.class);
    }

    @Override
    public List<GuiaCampoDto> getGuiasCampos() {
        return guiacamporepository.findAll().stream()
                .map(guia -> modelMapper.map(guia, GuiaCampoDto.class))
                .toList();
    }

    @Override
    public void eliminar(Long id) {
        if (guiacamporepository.existsById(id)) {
            guiacamporepository.deleteById(id);
        } else {
            throw new RuntimeException("No se encontró la guía de campo con ID: " + id);
        }
    }

    @Override
    public GuiaCampoDto actualizar(GuiaCampoDto guiadto) {
        Long id = guiadto.getIdGuia();
        if (id == null) {
            throw new RuntimeException("El ID de la guía de campo no puede ser nulo");
        }

        GuiaCampo guiaExistente = guiacamporepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la guía de campo con ID: " + id));

        guiaExistente.setNombreCampo(guiadto.getNombreCampo());
        guiaExistente.setTituloEs(guiadto.getTituloEs());
        guiaExistente.setDescripcionEs(guiadto.getDescripcionEs());
        guiaExistente.setEjemplos(guiadto.getEjemplos());
        guiaExistente.setRangoRecomendado(guiadto.getRangoRecomendado());
        guiaExistente.setUnidadMedida(guiadto.getUnidadMedida());

        GuiaCampo actualizado = guiacamporepository.save(guiaExistente);
        return modelMapper.map(actualizado, GuiaCampoDto.class);
    }

    @Override
    public GuiaCampoDto obtenerPorId(Long id) {
        GuiaCampo guia = guiacamporepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Guía de campo no encontrada con ID: " + id));
        return modelMapper.map(guia, GuiaCampoDto.class);
    }

    @Override
    public GuiaCampoDto obtenerPorNombreCampo(String nombreCampo) {
        GuiaCampo guia = guiacamporepository.findByNombreCampo(nombreCampo);
        if (guia == null) {
            throw new RuntimeException("Guía de campo no encontrada para: " + nombreCampo);
        }
        return modelMapper.map(guia, GuiaCampoDto.class);
    }
}
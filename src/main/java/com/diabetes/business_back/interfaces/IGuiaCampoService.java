package com.diabetes.business_back.interfaces;

import com.diabetes.business_back.dtos.GuiaCampoDto;

import java.util.List;

public interface IGuiaCampoService {
    public GuiaCampoDto grabarGuiaCampo(GuiaCampoDto guiadto);
    public List<GuiaCampoDto> getGuiasCampos();
    void eliminar(Long id);
    GuiaCampoDto actualizar(GuiaCampoDto guiadto);
    public GuiaCampoDto obtenerPorId(Long id);
    GuiaCampoDto obtenerPorNombreCampo(String nombreCampo);
}
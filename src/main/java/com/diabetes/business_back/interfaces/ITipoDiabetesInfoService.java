package com.diabetes.business_back.interfaces;

import com.diabetes.business_back.dtos.TipoDiabetesInfoDto;

import java.util.List;

public interface ITipoDiabetesInfoService {
    public TipoDiabetesInfoDto grabarTipoDiabetes(TipoDiabetesInfoDto tipodto);
    public List<TipoDiabetesInfoDto> getTiposDiabetes();
    void eliminar(Long id);
    TipoDiabetesInfoDto actualizar(TipoDiabetesInfoDto tipodto);
    public TipoDiabetesInfoDto obtenerPorId(Long id);
    TipoDiabetesInfoDto obtenerPorNombreEn(String nombreEn);
    List<TipoDiabetesInfoDto> obtenerTiposComunes();
}

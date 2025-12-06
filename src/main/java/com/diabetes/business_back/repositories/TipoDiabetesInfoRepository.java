package com.diabetes.business_back.repositories;

import com.diabetes.business_back.entities.TipoDiabetesInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoDiabetesInfoRepository extends JpaRepository<TipoDiabetesInfo, Long> {
    TipoDiabetesInfo findByNombreEn(String nombreEn);
    List<TipoDiabetesInfo> findByEsComun(Boolean esComun);
}

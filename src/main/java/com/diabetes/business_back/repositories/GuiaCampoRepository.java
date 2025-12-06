package com.diabetes.business_back.repositories;

import com.diabetes.business_back.entities.GuiaCampo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuiaCampoRepository extends JpaRepository<GuiaCampo, Long> {
    GuiaCampo findByNombreCampo(String nombreCampo);
}

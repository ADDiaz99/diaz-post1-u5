package com.universidad.reservaslabs.repository;

import com.universidad.reservaslabs.model.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {

    // Declarado por la guía pero sin uso todavía: el día que el catálogo exija
    // nombres únicos, esa regla justificará crear un LaboratorioService (ver
    // README, nota sobre LaboratorioController).
    boolean existsByNombreIgnoreCase(String nombre);
}

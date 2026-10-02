package com.universidad.reservaslabs.controller;

import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Excepción intencional a "el Controller nunca toca el Repository": el
 * catálogo de laboratorios es un CRUD sin reglas de negocio propias, y un
 * LaboratorioService que solo delegue sería un Service anémico. Ver README.
 */
@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController {

    private final LaboratorioRepository repo;

    public LaboratorioController(LaboratorioRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Laboratorio> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Laboratorio> obtener(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Laboratorio> crear(@RequestBody @Valid Laboratorio laboratorio) {
        laboratorio.setId(null); // POST siempre crea, nunca sobrescribe
        return ResponseEntity.status(201).body(repo.save(laboratorio));
    }
}

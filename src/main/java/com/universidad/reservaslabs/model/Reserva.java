package com.universidad.reservaslabs.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "laboratorio_id", nullable = false)
    private Laboratorio laboratorio;

    @Column(nullable = false)
    @NotBlank(message = "El nombre del solicitante no puede estar vacío")
    private String nombreSolicitante;

    // @NotBlank agregado: @Email por sí solo acepta null, y la columna es
    // NOT NULL, así que un correo vacío terminaba en un error 500 de base de
    // datos en lugar de un 400 de validación.
    @Column(nullable = false)
    @NotBlank(message = "El correo del solicitante es obligatorio")
    @Email(message = "El correo del solicitante debe ser válido")
    private String correoSolicitante;

    @Column(nullable = false)
    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    private LocalDateTime inicio;

    @Column(nullable = false)
    @NotNull(message = "La fecha y hora de fin es obligatoria")
    private LocalDateTime fin;

    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado;
}

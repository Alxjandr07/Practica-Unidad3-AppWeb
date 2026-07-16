package com.uteq.unidad3.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidad principal del PFC (cooperativa de transporte).
 * Es la entidad sobre la que se mide el impacto del cache-aside con Redis
 * (Anexo C, pregunta 1: Speedup S = Tsin / Tcon).
 *
 * Implementa Serializable porque el CacheManager (Redis) serializa el valor
 * en JSON via GenericJackson2JsonRedisSerializer (ver RedisConfig).
 */
@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(name = "anio_fabricacion", nullable = false)
    private Integer anioFabricacion;

    @Column(name = "capacidad_pasajeros", nullable = false)
    private Integer capacidadPasajeros;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_servicio", nullable = false, length = 20)
    private TipoServicio tipoServicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVehiculo estado;

    @Column(name = "conductor_asignado", length = 150)
    private String conductorAsignado;

    @Column(name = "ruta_asignada", length = 150)
    private String rutaAsignada;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (estado == null) estado = EstadoVehiculo.ACTIVO;
    }
}

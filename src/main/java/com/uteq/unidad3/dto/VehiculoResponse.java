package com.uteq.unidad3.dto;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import com.uteq.unidad3.domain.Vehiculo;

import java.time.LocalDateTime;

public record VehiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        Integer anioFabricacion,
        Integer capacidadPasajeros,
        TipoServicio tipoServicio,
        EstadoVehiculo estado,
        String conductorAsignado,
        String rutaAsignada,
        LocalDateTime fechaRegistro
) {
    public static VehiculoResponse desde(Vehiculo v) {
        return new VehiculoResponse(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(),
                v.getAnioFabricacion(), v.getCapacidadPasajeros(), v.getTipoServicio(),
                v.getEstado(), v.getConductorAsignado(), v.getRutaAsignada(), v.getFechaRegistro());
    }
}

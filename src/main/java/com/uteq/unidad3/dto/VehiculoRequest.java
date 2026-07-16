package com.uteq.unidad3.dto;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import jakarta.validation.constraints.*;

public record VehiculoRequest(
        @NotBlank @Size(max = 10) String placa,
        @NotBlank @Size(max = 50) String marca,
        @NotBlank @Size(max = 50) String modelo,
        @NotNull @Min(1980) Integer anioFabricacion,
        @NotNull @Min(1) Integer capacidadPasajeros,
        @NotNull TipoServicio tipoServicio,
        EstadoVehiculo estado,
        String conductorAsignado,
        String rutaAsignada
) {}

package com.uteq.unidad3.service;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import com.uteq.unidad3.dto.VehiculoRequest;
import com.uteq.unidad3.dto.VehiculoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehiculoService {

    Page<VehiculoResponse> buscar(EstadoVehiculo estado, TipoServicio tipoServicio, String texto, Pageable pageable);

    VehiculoResponse obtenerPorId(Long id);

    VehiculoResponse crear(VehiculoRequest request);

    VehiculoResponse actualizar(Long id, VehiculoRequest request);

    void eliminar(Long id);
}

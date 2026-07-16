package com.uteq.unidad3.repository;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import com.uteq.unidad3.domain.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * JpaSpecificationExecutor habilita filtros dinamicos combinables (estado, tipo_servicio,
 * texto libre sobre placa/marca/modelo) sin explotar en un metodo por cada combinacion,
 * manteniendo la paginacion (Pageable) y el ordenamiento dinamico (Sort) que trae Pageable.
 */
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long>,
        JpaSpecificationExecutor<Vehiculo> {

    Optional<Vehiculo> findByPlaca(String placa);

    boolean existsByPlaca(String placa);

    Page<Vehiculo> findByEstado(EstadoVehiculo estado, Pageable pageable);

    Page<Vehiculo> findByTipoServicio(TipoServicio tipoServicio, Pageable pageable);

    long countByEstado(EstadoVehiculo estado);
}

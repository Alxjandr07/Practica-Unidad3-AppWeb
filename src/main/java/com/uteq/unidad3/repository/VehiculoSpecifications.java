package com.uteq.unidad3.repository;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import com.uteq.unidad3.domain.Vehiculo;
import org.springframework.data.jpa.domain.Specification;

/**
 * Construye el filtro combinado (estado + tipoServicio + texto libre) que usa
 * VehiculoServiceImpl#buscar(...). Cada criterio se agrega solo si viene informado,
 * de forma que la consulta final refleje exactamente los filtros que el cliente envio.
 */
public final class VehiculoSpecifications {

    private VehiculoSpecifications() {}

    public static Specification<Vehiculo> conFiltros(EstadoVehiculo estado,
                                                       TipoServicio tipoServicio,
                                                       String texto) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();

            if (estado != null) {
                predicate = cb.and(predicate, cb.equal(root.get("estado"), estado));
            }
            if (tipoServicio != null) {
                predicate = cb.and(predicate, cb.equal(root.get("tipoServicio"), tipoServicio));
            }
            if (texto != null && !texto.isBlank()) {
                String like = "%" + texto.toLowerCase() + "%";
                var placaLike = cb.like(cb.lower(root.get("placa")), like);
                var marcaLike = cb.like(cb.lower(root.get("marca")), like);
                var modeloLike = cb.like(cb.lower(root.get("modelo")), like);
                predicate = cb.and(predicate, cb.or(placaLike, marcaLike, modeloLike));
            }
            return predicate;
        };
    }
}

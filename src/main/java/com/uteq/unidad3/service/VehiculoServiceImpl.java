package com.uteq.unidad3.service;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import com.uteq.unidad3.domain.Vehiculo;
import com.uteq.unidad3.dto.VehiculoRequest;
import com.uteq.unidad3.dto.VehiculoResponse;
import com.uteq.unidad3.exception.RecursoDuplicadoException;
import com.uteq.unidad3.exception.RecursoNoEncontradoException;
import com.uteq.unidad3.repository.VehiculoRepository;
import com.uteq.unidad3.repository.VehiculoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arquitectura en capas: Controller -> Service -> Repository -> Entity.
 * Este service es la capa donde se aplica el patron Cache-Aside (Redis) sobre
 * las operaciones de lectura, y la invalidacion (CacheEvict) sobre las de escritura.
 *
 * Cache-Aside, flujo de lectura (ver buscar/obtenerPorId):
 *   1. La app pregunta primero a Redis (cache "vehiculosListado" / "vehiculosPorId").
 *   2. Si hay HIT -> se retorna directo desde Redis (rapido, no toca Postgres).
 *   3. Si hay MISS -> se consulta Postgres, y el resultado se guarda en Redis para
 *      la proxima peticion equivalente. Esto es exactamente lo que hace @Cacheable:
 *      intercepta el metodo, resuelve la clave, y decide si ejecuta el cuerpo o no.
 *
 * Cache-Aside, flujo de escritura (crear/actualizar/eliminar):
 *   Se usa @CacheEvict para borrar las entradas que quedarian desactualizadas.
 *   "vehiculosListado" se vacia completo (allEntries = true) porque cualquier alta/baja
 *   puede cambiar el contenido de CUALQUIER pagina/filtro cacheado; "vehiculosPorId"
 *   solo evict la entrada puntual del id afectado.
 */
@Service
@RequiredArgsConstructor
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    @Override
    @Cacheable(cacheNames = "vehiculosListado", keyGenerator = "vehiculoListadoKeyGenerator")
    @Transactional(readOnly = true)
    public Page<VehiculoResponse> buscar(EstadoVehiculo estado, TipoServicio tipoServicio,
                                          String texto, Pageable pageable) {
        var spec = VehiculoSpecifications.conFiltros(estado, tipoServicio, texto);
        Page<VehiculoResponse> page = vehiculoRepository.findAll(spec, pageable).map(VehiculoResponse::desde);
        return new com.uteq.unidad3.dto.RestPageImpl<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    @Override
    @Cacheable(cacheNames = "vehiculosPorId", key = "#id")
    @Transactional(readOnly = true)
    public VehiculoResponse obtenerPorId(Long id) {
        return vehiculoRepository.findById(id)
                .map(VehiculoResponse::desde)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo no encontrado, id=" + id));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "vehiculosListado", allEntries = true)
    })
    @Transactional
    public VehiculoResponse crear(VehiculoRequest request) {
        if (vehiculoRepository.existsByPlaca(request.placa())) {
            throw new RecursoDuplicadoException("Ya existe un vehiculo con placa " + request.placa());
        }
        Vehiculo vehiculo = mapear(new Vehiculo(), request);
        return VehiculoResponse.desde(vehiculoRepository.save(vehiculo));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "vehiculosPorId", key = "#id"),
            @CacheEvict(cacheNames = "vehiculosListado", allEntries = true)
    })
    @Transactional
    public VehiculoResponse actualizar(Long id, VehiculoRequest request) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo no encontrado, id=" + id));

        if (!vehiculo.getPlaca().equalsIgnoreCase(request.placa())
                && vehiculoRepository.existsByPlaca(request.placa())) {
            throw new RecursoDuplicadoException("Ya existe un vehiculo con placa " + request.placa());
        }

        mapear(vehiculo, request);
        return VehiculoResponse.desde(vehiculoRepository.save(vehiculo));
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "vehiculosPorId", key = "#id"),
            @CacheEvict(cacheNames = "vehiculosListado", allEntries = true)
    })
    @Transactional
    public void eliminar(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Vehiculo no encontrado, id=" + id);
        }
        vehiculoRepository.deleteById(id);
    }

    private Vehiculo mapear(Vehiculo vehiculo, VehiculoRequest request) {
        vehiculo.setPlaca(request.placa().toUpperCase());
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setAnioFabricacion(request.anioFabricacion());
        vehiculo.setCapacidadPasajeros(request.capacidadPasajeros());
        vehiculo.setTipoServicio(request.tipoServicio());
        vehiculo.setEstado(request.estado() != null ? request.estado() : EstadoVehiculo.ACTIVO);
        vehiculo.setConductorAsignado(request.conductorAsignado());
        vehiculo.setRutaAsignada(request.rutaAsignada());
        return vehiculo;
    }
}

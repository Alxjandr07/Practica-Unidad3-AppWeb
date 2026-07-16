package com.uteq.unidad3.service;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import com.uteq.unidad3.domain.Vehiculo;
import com.uteq.unidad3.dto.VehiculoRequest;
import com.uteq.unidad3.exception.RecursoDuplicadoException;
import com.uteq.unidad3.exception.RecursoNoEncontradoException;
import com.uteq.unidad3.repository.VehiculoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias puras (Mockito) de la logica de negocio en VehiculoServiceImpl,
 * sin levantar contexto de Spring ni base de datos real. Complementan las pruebas
 * de integracion de VehiculoRepositoryTest (capa Repository con H2).
 *
 * Nota (Anexo C, pregunta 4): estas pruebas NO verifican el comportamiento real de
 * @Cacheable/@CacheEvict (eso requiere un ApplicationContext con CacheManager activo,
 * p.ej. @SpringBootTest + Testcontainers para Redis); solo prueban las reglas de
 * negocio (placa duplicada, recurso no encontrado, mapeo de campos).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehiculoServiceImpl")
class VehiculoServiceImplTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private VehiculoServiceImpl vehiculoService;

    private VehiculoRequest requestValido() {
        return new VehiculoRequest("PQ1234", "Hino", "AK8", 2021, 30,
                TipoServicio.URBANO, EstadoVehiculo.ACTIVO, "Jose Vera", "Quevedo - Babahoyo");
    }

    @Test
    @DisplayName("crear() lanza RecursoDuplicadoException si la placa ya existe")
    void crearConPlacaDuplicada() {
        when(vehiculoRepository.existsByPlaca("PQ1234")).thenReturn(true);

        assertThatThrownBy(() -> vehiculoService.crear(requestValido()))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("PQ1234");

        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    @DisplayName("crear() guarda el vehiculo cuando la placa es unica")
    void crearVehiculoValido() {
        when(vehiculoRepository.existsByPlaca("PQ1234")).thenReturn(false);
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> {
            Vehiculo v = inv.getArgument(0);
            v.setId(1L);
            return v;
        });

        var respuesta = vehiculoService.crear(requestValido());

        assertThat(respuesta.id()).isEqualTo(1L);
        assertThat(respuesta.placa()).isEqualTo("PQ1234");
        verify(vehiculoRepository).save(any(Vehiculo.class));
    }

    @Test
    @DisplayName("obtenerPorId() lanza RecursoNoEncontradoException si no existe")
    void obtenerPorIdInexistente() {
        when(vehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehiculoService.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("eliminar() lanza RecursoNoEncontradoException si el id no existe")
    void eliminarInexistente() {
        when(vehiculoRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> vehiculoService.eliminar(1L))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(vehiculoRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("actualizar() permite conservar la misma placa sin disparar duplicado")
    void actualizarConservandoPlaca() {
        Vehiculo existente = Vehiculo.builder()
                .id(5L).placa("PQ1234").marca("Hino").modelo("AK8")
                .anioFabricacion(2019).capacidadPasajeros(28)
                .tipoServicio(TipoServicio.URBANO).estado(EstadoVehiculo.ACTIVO)
                .build();

        when(vehiculoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(inv -> inv.getArgument(0));

        var respuesta = vehiculoService.actualizar(5L, requestValido());

        assertThat(respuesta.capacidadPasajeros()).isEqualTo(30); // valor actualizado del request
        verify(vehiculoRepository, never()).existsByPlaca(any());
    }
}

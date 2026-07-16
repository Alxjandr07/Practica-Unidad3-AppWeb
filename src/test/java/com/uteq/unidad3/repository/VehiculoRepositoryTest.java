package com.uteq.unidad3.repository;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import com.uteq.unidad3.domain.Vehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de la capa Repository con H2 en memoria (perfil "test"), tal como pide
 * la guia (equivalente al enfoque de SQLite-en-memoria de la version Laravel).
 * Cubren: persistencia basica, paginacion, filtros por Specification y unicidad de placa.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("VehiculoRepository")
class VehiculoRepositoryTest {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @BeforeEach
    void limpiar() {
        vehiculoRepository.deleteAll();
    }

    private Vehiculo nuevoVehiculo(String placa, EstadoVehiculo estado, TipoServicio tipo) {
        return Vehiculo.builder()
                .placa(placa)
                .marca("Hino")
                .modelo("AK8")
                .anioFabricacion(2020)
                .capacidadPasajeros(30)
                .tipoServicio(tipo)
                .estado(estado)
                .build();
    }

    @Test
    @DisplayName("guarda y recupera un vehiculo por id")
    void guardarYRecuperar() {
        Vehiculo guardado = vehiculoRepository.save(nuevoVehiculo("PQ1234", EstadoVehiculo.ACTIVO, TipoServicio.URBANO));

        assertThat(guardado.getId()).isNotNull();
        assertThat(vehiculoRepository.findById(guardado.getId())).isPresent();
    }

    @Test
    @DisplayName("no permite dos vehiculos con la misma placa (constraint unique)")
    void placaUnica() {
        vehiculoRepository.save(nuevoVehiculo("PQ9999", EstadoVehiculo.ACTIVO, TipoServicio.URBANO));

        assertThat(vehiculoRepository.existsByPlaca("PQ9999")).isTrue();
        assertThat(vehiculoRepository.existsByPlaca("PQ0000")).isFalse();
    }

    @Test
    @DisplayName("pagina correctamente los resultados respetando el tamano de pagina")
    void paginacion() {
        for (int i = 0; i < 15; i++) {
            vehiculoRepository.save(nuevoVehiculo("PQ10" + i, EstadoVehiculo.ACTIVO, TipoServicio.URBANO));
        }

        var pagina = vehiculoRepository.findAll(PageRequest.of(0, 10, Sort.by("placa")));

        assertThat(pagina.getContent()).hasSize(10);
        assertThat(pagina.getTotalElements()).isEqualTo(15);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("filtra por estado usando el metodo derivado paginado")
    void filtroPorEstado() {
        vehiculoRepository.save(nuevoVehiculo("PQ0001", EstadoVehiculo.ACTIVO, TipoServicio.URBANO));
        vehiculoRepository.save(nuevoVehiculo("PQ0002", EstadoVehiculo.MANTENIMIENTO, TipoServicio.URBANO));
        vehiculoRepository.save(nuevoVehiculo("PQ0003", EstadoVehiculo.ACTIVO, TipoServicio.ESCOLAR));

        var activos = vehiculoRepository.findByEstado(EstadoVehiculo.ACTIVO, PageRequest.of(0, 10));

        assertThat(activos.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("combina filtros (estado + tipoServicio) usando Specification")
    void filtroCombinadoConSpecification() {
        vehiculoRepository.save(nuevoVehiculo("PQ0011", EstadoVehiculo.ACTIVO, TipoServicio.URBANO));
        vehiculoRepository.save(nuevoVehiculo("PQ0012", EstadoVehiculo.ACTIVO, TipoServicio.ESCOLAR));
        vehiculoRepository.save(nuevoVehiculo("PQ0013", EstadoVehiculo.MANTENIMIENTO, TipoServicio.URBANO));

        var spec = VehiculoSpecifications.conFiltros(EstadoVehiculo.ACTIVO, TipoServicio.URBANO, null);
        var resultado = vehiculoRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getPlaca()).isEqualTo("PQ0011");
    }

    @Test
    @DisplayName("el filtro de texto libre busca en placa, marca y modelo (case-insensitive)")
    void filtroPorTextoLibre() {
        vehiculoRepository.save(nuevoVehiculo("PQ7777", EstadoVehiculo.ACTIVO, TipoServicio.URBANO));

        var spec = VehiculoSpecifications.conFiltros(null, null, "hino");
        var resultado = vehiculoRepository.findAll(spec, PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }
}

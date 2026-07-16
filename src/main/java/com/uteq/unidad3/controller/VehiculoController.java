package com.uteq.unidad3.controller;

import com.uteq.unidad3.domain.EstadoVehiculo;
import com.uteq.unidad3.domain.TipoServicio;
import com.uteq.unidad3.dto.VehiculoRequest;
import com.uteq.unidad3.dto.VehiculoResponse;
import com.uteq.unidad3.service.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CRUD completo con paginacion (Pageable), filtros multiples (estado, tipoServicio,
 * texto libre) y ordenamiento dinamico via el parametro estandar "sort" de Spring
 * Data (ej: GET /api/vehiculos?sort=marca,asc&sort=anioFabricacion,desc).
 */
@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    @GetMapping
    public ResponseEntity<Page<VehiculoResponse>> listar(
            @RequestParam(required = false) EstadoVehiculo estado,
            @RequestParam(required = false) TipoServicio tipoServicio,
            @RequestParam(required = false) String texto,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(vehiculoService.buscar(estado, tipoServicio, texto, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponse crear(@Valid @RequestBody VehiculoRequest request) {
        return vehiculoService.crear(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody VehiculoRequest request) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
    }
}

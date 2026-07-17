import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { VehiculoService } from '../../services/vehiculo/vehiculo.service';
import { Vehiculo } from '../../models/vehiculo.model';

@Component({
  selector: 'app-vehiculo-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './vehiculo-form.component.html',
  styleUrls: ['./vehiculo-form.component.css']
})
export class VehiculoFormComponent implements OnInit {
  vehiculoForm: FormGroup;
  isEditMode: boolean = false;
  vehiculoId: number | null = null;
  loading: boolean = false;
  errorMsg: string = '';

  private fb = inject(FormBuilder);
  private vehiculoService = inject(VehiculoService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  constructor() {
    this.vehiculoForm = this.fb.group({
      placa: ['', [Validators.required, Validators.pattern(/^[A-Z]{3}-\d{3,4}$/)]],
      marca: ['', Validators.required],
      modelo: ['', Validators.required],
      anioFabricacion: [new Date().getFullYear(), [Validators.required, Validators.min(1900)]],
      capacidadPasajeros: [1, [Validators.required, Validators.min(1)]],
      tipoServicio: ['URBANO', Validators.required],
      estado: ['ACTIVO'] // Por defecto al crear
    });
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.isEditMode = true;
      this.vehiculoId = +idParam;
      this.cargarVehiculo(this.vehiculoId);
    }
  }

  cargarVehiculo(id: number): void {
    this.vehiculoService.getVehiculoById(id).subscribe({
      next: (v) => {
        this.vehiculoForm.patchValue(v);
      },
      error: (err) => console.error('Error al cargar vehículo', err)
    });
  }

  onSubmit(): void {
    if (this.vehiculoForm.invalid) {
      this.vehiculoForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const vehiculoData: Vehiculo = this.vehiculoForm.value;

    if (this.isEditMode && this.vehiculoId) {
      this.vehiculoService.updateVehiculo(this.vehiculoId, vehiculoData).subscribe({
        next: () => this.router.navigate(['/vehiculos']),
        error: (err) => {
          this.loading = false;
          this.errorMsg = 'Error al actualizar: ' + err.message;
        }
      });
    } else {
      this.vehiculoService.createVehiculo(vehiculoData).subscribe({
        next: () => this.router.navigate(['/vehiculos']),
        error: (err) => {
          this.loading = false;
          this.errorMsg = 'Error al crear: ' + err.message;
        }
      });
    }
  }
}

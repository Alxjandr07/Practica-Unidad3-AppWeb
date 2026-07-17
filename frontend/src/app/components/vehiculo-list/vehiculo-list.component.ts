import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VehiculoService } from '../../services/vehiculo/vehiculo.service';
import { AuthService } from '../../services/auth/auth.service';
import { Vehiculo } from '../../models/vehiculo.model';

@Component({
  selector: 'app-vehiculo-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './vehiculo-list.component.html',
  styleUrls: ['./vehiculo-list.component.css']
})
export class VehiculoListComponent implements OnInit {
  vehiculos: Vehiculo[] = [];
  
  // Paginación
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;
  
  // Filtros
  filtroEstado: string = '';
  filtroSort: string = 'id,asc';

  private vehiculoService = inject(VehiculoService);
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit(): void {
    this.cargarVehiculos();
  }

  cargarVehiculos(): void {
    const estado = this.filtroEstado ? this.filtroEstado : undefined;
    this.vehiculoService.getVehiculos(this.currentPage, this.pageSize, estado, this.filtroSort)
      .subscribe({
        next: (res) => {
          this.vehiculos = res.content;
          this.totalElements = res.totalElements;
          this.totalPages = res.totalPages;
        },
        error: (err) => {
          console.error('Error al cargar vehículos', err);
          if (err.status === 401 || err.status === 403) {
             localStorage.removeItem('loggedIn');
             this.authService.isLoggedIn.set(false);
             this.router.navigate(['/login']);
          }
        }
      });
  }

  cambiarPagina(nuevaPagina: number): void {
    if (nuevaPagina >= 0 && nuevaPagina < this.totalPages) {
      this.currentPage = nuevaPagina;
      this.cargarVehiculos();
    }
  }

  aplicarFiltro(): void {
    this.currentPage = 0; // reset al inicio
    this.cargarVehiculos();
  }

  eliminarVehiculo(id: number | undefined): void {
    if (id && confirm('¿Está seguro de eliminar este vehículo?')) {
      this.vehiculoService.deleteVehiculo(id).subscribe({
        next: () => this.cargarVehiculos(),
        error: (err) => console.error(err)
      });
    }
  }
  
  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login'])
    });
  }
}

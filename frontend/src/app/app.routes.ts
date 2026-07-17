import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { VehiculoListComponent } from './components/vehiculo-list/vehiculo-list.component';
import { VehiculoFormComponent } from './components/vehiculo-form/vehiculo-form.component';
import { authGuard } from './guards/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'vehiculos', component: VehiculoListComponent, canActivate: [authGuard] },
  { path: 'vehiculos/nuevo', component: VehiculoFormComponent, canActivate: [authGuard] },
  { path: 'vehiculos/editar/:id', component: VehiculoFormComponent, canActivate: [authGuard] },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

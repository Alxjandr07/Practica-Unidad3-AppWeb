import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { authGuard } from './guards/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  // { path: 'vehiculos', component: VehiculosComponent, canActivate: [authGuard] }, // Lo añadiremos luego
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

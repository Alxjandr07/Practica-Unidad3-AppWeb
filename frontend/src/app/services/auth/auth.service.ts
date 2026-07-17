import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  
  // Usamos Signal para manejar el estado reactivo del usuario logueado en Angular 17+
  public isLoggedIn = signal<boolean>(false);

  constructor(private http: HttpClient) { 
    // Al recargar, si no sabemos, asumimos false, ya que no tenemos acceso a la cookie HttpOnly
    // En un sistema real se haría un ping a un endpoint /me
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials, { 
      withCredentials: true,
      observe: 'response'
    }).pipe(
      tap(() => {
        this.isLoggedIn.set(true);
        localStorage.setItem('loggedIn', 'true');
      })
    );
  }

  logout(): Observable<any> {
    return this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => {
        this.isLoggedIn.set(false);
        localStorage.removeItem('loggedIn');
      }),
      catchError(() => {
        this.isLoggedIn.set(false);
        localStorage.removeItem('loggedIn');
        return of(null);
      })
    );
  }

  checkAuthStatus(): boolean {
    const logged = localStorage.getItem('loggedIn') === 'true';
    this.isLoggedIn.set(logged);
    return logged;
  }
}

package com.uteq.unidad3.service;

import com.uteq.unidad3.dto.LoginRequest;
import com.uteq.unidad3.security.CookieProperties;
import com.uteq.unidad3.security.CookieUtil;
import com.uteq.unidad3.security.JwtService;
import com.uteq.unidad3.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Orquesta el ciclo de autenticacion stateless completo:
 *  login  -> valida credenciales, genera JWT, lo entrega como cookie HttpOnly.
 *  logout -> extrae el jti del token vigente y lo agrega a la blacklist de Redis,
 *            ademas de expirar la cookie en el cliente. Esto demuestra que el logout
 *            invalida efectivamente el token: aunque alguien capture el JWT antes del
 *            logout, JwtAuthFilter lo rechazara despues porque su jti esta en blacklist.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final CookieProperties cookieProperties;

    public UserDetails login(LoginRequest request, HttpServletResponse response) {
        var authToken = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        var authentication = authenticationManager.authenticate(authToken); // lanza BadCredentialsException si falla

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtService.generarToken(userDetails);

        response.addCookie(CookieUtil.crearCookieToken(
                cookieProperties.cookieName(), jwt,
                cookieProperties.expirationSeconds(), cookieProperties.cookieSecure()));

        return userDetails;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.leerCookie(request, cookieProperties.cookieName()).ifPresent(token -> {
            try {
                String jti = jwtService.extraerJti(token);
                long ttlRestante = jwtService.msRestantes(token);
                blacklistService.invalidar(jti, ttlRestante);
            } catch (Exception ignored) {
                // token ya invalido/corrupto: no hay nada que revocar, igual se limpia la cookie abajo
            }
        });

        response.addCookie(CookieUtil.crearCookieExpirada(cookieProperties.cookieName(), cookieProperties.cookieSecure()));
    }
}

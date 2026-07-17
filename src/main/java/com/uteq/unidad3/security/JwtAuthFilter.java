package com.uteq.unidad3.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filtro que se ejecuta una vez por request:
 *  1) Lee el JWT desde la cookie HttpOnly (no desde Authorization header: el
 *     frontend Angular no necesita manipular el token).
 *  2) Verifica firma/expiracion.
 *  3) Consulta la blacklist en Redis (jti) -> si esta invalidado, el request
 *     sigue como anonimo (equivalente a "sesion cerrada").
 *  4) Si todo es valido, puebla el SecurityContext para ese request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService blacklistService;
    private final UserDetailsService userDetailsService;
    private final CookieProperties cookieProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        Optional<String> tokenOpt = CookieUtil.leerCookie(request, cookieProperties.cookieName());
        
        System.out.println("=== JWT FILTER: Request a " + request.getRequestURI() + " | Cookie presente? " + tokenOpt.isPresent() + " ===");

        if (tokenOpt.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = tokenOpt.get();
            try {
                String jti = jwtService.extraerJti(token);

                if (!blacklistService.estaInvalidado(jti)) {
                    String username = jwtService.extraerUsername(token);
                    var userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.esTokenValido(token, userDetails)) {
                        var authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception ex) {
                // token corrupto/expirado/firma invalida -> se continua sin autenticar
                System.err.println("=== ERROR EN JWT AUTH FILTER ===");
                ex.printStackTrace();
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}

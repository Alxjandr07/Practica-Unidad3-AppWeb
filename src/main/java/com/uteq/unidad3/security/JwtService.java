package com.uteq.unidad3.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

/**
 * Servicio de firma/verificacion de JWT.
 *
 * Cada token incluye un claim "jti" (JWT ID) unico. Ese jti es la clave que se usa
 * en TokenBlacklistService para invalidar un token especifico en logout, sin tener
 * que mantener estado de sesion en el servidor (el servidor sigue siendo stateless:
 * lo unico que se guarda en Redis es la "lista negra" de tokens revocados, con TTL
 * igual al tiempo de vida restante del token).
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secretBase64,
                       @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretBase64));
        this.expirationMs = expirationMs;
    }

    public String generarToken(UserDetails userDetails) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())         // jti -> usado para blacklist
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().toString())
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(signingKey)
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public String extraerJti(String token) {
        return extraerClaim(token, Claims::getId);
    }

    public Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        String username = extraerUsername(token);
        return username.equals(userDetails.getUsername()) && !haExpirado(token);
    }

    /** Milisegundos que le quedan de vida al token; usado como TTL de la entrada en la blacklist. */
    public long msRestantes(String token) {
        long restante = extraerExpiracion(token).getTime() - System.currentTimeMillis();
        return Math.max(restante, 0);
    }

    private boolean haExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    private <T> T extraerClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}

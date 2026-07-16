package com.uteq.unidad3.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Blacklist de JTI en Redis (String: jti -> "revocado", con TTL = tiempo de vida
 * restante del token). Esto es lo que permite invalidar efectivamente un JWT en
 * logout, sin romper el caracter stateless de la autenticacion: el servidor no
 * guarda sesiones, solo consulta si el jti recibido esta en la lista negra.
 *
 * Clave Redis: "jwt:blacklist:{jti}"
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void invalidar(String jti, long ttlMs) {
        if (ttlMs <= 0) return; // el token ya expiro por si solo, no hace falta guardarlo
        redisTemplate.opsForValue().set(PREFIX + jti, "revocado", Duration.ofMillis(ttlMs));
    }

    public boolean estaInvalidado(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
    }
}

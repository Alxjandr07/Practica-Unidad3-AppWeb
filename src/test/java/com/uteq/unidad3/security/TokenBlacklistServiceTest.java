package com.uteq.unidad3.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifica que TokenBlacklistService use correctamente la API de Redis (StringRedisTemplate)
 * para demostrar que el logout invalida efectivamente el token: tras invalidar un jti,
 * estaInvalidado(jti) debe reportar true.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService")
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("invalidar() escribe la clave jwt:blacklist:{jti} con el TTL indicado")
    void invalidarEscribeConTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        service.invalidar("jti-123", 5000L);

        verify(valueOperations).set(eq("jwt:blacklist:jti-123"), eq("revocado"), eq(Duration.ofMillis(5000L)));
    }

    @Test
    @DisplayName("invalidar() no escribe nada si el token ya expiro (ttl <= 0)")
    void invalidarConTtlNegativoNoEscribe() {
        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        service.invalidar("jti-expirado", 0L);

        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("estaInvalidado() consulta la existencia de la clave en Redis")
    void estaInvalidadoConsultaExistencia() {
        when(redisTemplate.hasKey("jwt:blacklist:jti-revocado")).thenReturn(true);
        when(redisTemplate.hasKey("jwt:blacklist:jti-vigente")).thenReturn(false);

        TokenBlacklistService service = new TokenBlacklistService(redisTemplate);

        assertThat(service.estaInvalidado("jti-revocado")).isTrue();
        assertThat(service.estaInvalidado("jti-vigente")).isFalse();
    }
}

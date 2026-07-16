package com.uteq.unidad3.security;

import com.uteq.unidad3.domain.Rol;
import com.uteq.unidad3.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService")
class JwtServiceTest {

    // Mismo secreto que en application.yml (Base64 de 32+ bytes), solo para el entorno de test.
    private static final String SECRET = "c2VjcmV0by11bmlkYWQzLXV0ZXEtYXBsaWNhY2lvbmVzLXdlYi1jYW1iaWFyLWVuLXByb2R1Y2Npb24=";

    private final JwtService jwtService = new JwtService(SECRET, 60_000); // 60s de vida

    private Usuario usuarioDePrueba() {
        return Usuario.builder()
                .id(1L).username("admin").passwordHash("hash")
                .nombreCompleto("Administrador").email("admin@uteq.edu.ec")
                .rol(Rol.ADMIN).activo(true).fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("genera un token valido cuyo subject coincide con el username")
    void generarYValidarToken() {
        Usuario usuario = usuarioDePrueba();
        String token = jwtService.generarToken(usuario);

        assertThat(jwtService.extraerUsername(token)).isEqualTo("admin");
        assertThat(jwtService.esTokenValido(token, usuario)).isTrue();
    }

    @Test
    @DisplayName("cada token generado tiene un jti (JWT ID) unico")
    void jtiUnicoPorToken() {
        Usuario usuario = usuarioDePrueba();
        String token1 = jwtService.generarToken(usuario);
        String token2 = jwtService.generarToken(usuario);

        assertThat(jwtService.extraerJti(token1)).isNotEqualTo(jwtService.extraerJti(token2));
    }

    @Test
    @DisplayName("msRestantes() retorna un valor positivo antes de expirar")
    void msRestantesPositivoAntesDeExpirar() {
        String token = jwtService.generarToken(usuarioDePrueba());
        assertThat(jwtService.msRestantes(token)).isGreaterThan(0);
    }
}

package com.uteq.unidad3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuracion de Redis para el patron Cache-Aside (ver VehiculoServiceImpl):
 *  - lectura: la app consulta primero Redis; si hay MISS, consulta Postgres y
 *    escribe el resultado en Redis (@Cacheable).
 *  - escritura: al crear/actualizar/eliminar un Vehiculo se invalidan las entradas
 *    afectadas (@CacheEvict) para no servir datos obsoletos (evita el problema
 *    clasico de cache-aside de inconsistencia tras una escritura).
 *
 * Cada "cache" logico tiene su propio TTL, pensado por tipo de dato cacheado:
 *  - vehiculosPorId: entidad individual, cambia poco -> TTL mas largo.
 *  - vehiculosListado: resultado de listados paginados/filtrados, cambia mas
 *    seguido (nuevas altas) -> TTL mas corto para acotar la ventana de staleness.
 */
@Configuration
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);

        var jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configPorCache = Stream.of(
                Map.entry("vehiculosPorId", base.entryTtl(Duration.ofMinutes(30))),
                Map.entry("vehiculosListado", base.entryTtl(Duration.ofMinutes(5)))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, HashMap::new));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(configPorCache)
                .build();
    }

    /**
     * Genera una clave de cache legible y determinista para VehiculoServiceImpl#buscar(...),
     * incluyendo pagina/tamano/orden y los filtros, para que combinaciones distintas de
     * filtros/paginacion no colisionen entre si en Redis.
     */
    @Bean
    public KeyGenerator vehiculoListadoKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder(method.getName());
            for (Object p : params) {
                sb.append(':').append(p == null ? "null" : p.toString());
            }
            return sb.toString();
        };
    }
}

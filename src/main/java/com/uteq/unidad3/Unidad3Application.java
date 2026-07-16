package com.uteq.unidad3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Practica Experimental Unidad III - Aplicaciones Web [111] - UTEQ.
 * Docente: Ing. Guerrero Ulloa Gleiston Ciceron.
 *
 * Stack: Java 21 / Spring Boot 3.3 / PostgreSQL 16 / Redis 7.
 * Cubre: gestion de estado stateless (JWT + blacklist Redis),
 * ORM con Spring Data JPA, arquitectura en capas y cache-aside con Redis.
 */
@SpringBootApplication
@EnableCaching
public class Unidad3Application {
    public static void main(String[] args) {
        SpringApplication.run(Unidad3Application.class, args);
    }
}

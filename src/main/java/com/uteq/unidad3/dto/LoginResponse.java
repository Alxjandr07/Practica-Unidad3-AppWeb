package com.uteq.unidad3.dto;

/** El token NO se retorna en el body a proposito: viaja unicamente como cookie HttpOnly. */
public record LoginResponse(String username, String rol, String mensaje) {}

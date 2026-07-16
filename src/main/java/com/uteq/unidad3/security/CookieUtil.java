package com.uteq.unidad3.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/** Utilidades para leer/escribir el JWT como cookie HttpOnly (no accesible desde JS, mitiga XSS). */
public final class CookieUtil {

    private CookieUtil() {}

    public static Cookie crearCookieToken(String nombre, String valor, int maxAgeSegundos, boolean secure) {
        Cookie cookie = new Cookie(nombre, valor);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);       // true en produccion (requiere HTTPS)
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSegundos);
        cookie.setAttribute("SameSite", "Strict"); // mitiga CSRF
        return cookie;
    }

    public static Cookie crearCookieExpirada(String nombre, boolean secure) {
        return crearCookieToken(nombre, "", 0, secure);
    }

    public static Optional<String> leerCookie(HttpServletRequest request, String nombre) {
        if (request.getCookies() == null) return Optional.empty();
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(nombre)) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}

package com.uteq.unidad3.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Envuelve las propiedades app.jwt.cookie-name / app.jwt.cookie-secure de application.yml. */
@Component
public class CookieProperties {

    private final String cookieName;
    private final boolean cookieSecure;
    private final int expirationSeconds;

    public CookieProperties(@Value("${app.jwt.cookie-name}") String cookieName,
                             @Value("${app.jwt.cookie-secure}") boolean cookieSecure,
                             @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.expirationSeconds = (int) (expirationMs / 1000);
    }

    public String cookieName() { return cookieName; }
    public boolean cookieSecure() { return cookieSecure; }
    public int expirationSeconds() { return expirationSeconds; }
}

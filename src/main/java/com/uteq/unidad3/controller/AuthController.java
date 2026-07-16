package com.uteq.unidad3.controller;

import com.uteq.unidad3.dto.LoginRequest;
import com.uteq.unidad3.dto.LoginResponse;
import com.uteq.unidad3.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletResponse response) {
        UserDetails userDetails = authService.login(request, response);
        String rol = userDetails.getAuthorities().stream()
                .findFirst().map(GrantedAuthority::getAuthority).orElse("DESCONOCIDO");

        return ResponseEntity.ok(new LoginResponse(userDetails.getUsername(), rol, "Autenticacion exitosa"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }
}

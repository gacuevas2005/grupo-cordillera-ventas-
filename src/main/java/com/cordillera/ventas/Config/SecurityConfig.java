package com.cordillera.ventas.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // ✅ Sin estado: el microservicio no guarda sesiones propias
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Todas las rutas de ventas son públicas internamente.
                        // La seguridad real la gestiona el BFF con JWT antes de llegar aquí.
                        .requestMatchers("/api/ventas/**", "/api/datos/ventas/**", "/error").permitAll()
                        .anyRequest().permitAll()
                )
                // ✅ CORRECCIÓN CRÍTICA: Se elimina .anonymous(anonymous -> anonymous.disable())
                // Esa línea hacía que Spring rechazara con 403 cualquier petición interna del BFF
                // que llegara sin un usuario autenticado en el SecurityContext del propio microservicio,
                // aunque la ruta estuviera en permitAll(). El BFF ya valida el JWT — este MS no necesita
                // autenticación propia para sus rutas internas.
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
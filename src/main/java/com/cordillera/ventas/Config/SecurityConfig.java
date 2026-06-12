package com.cordillera.ventas.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitamos CSRF para poder operar libremente entre microservicios
                .csrf(csrf -> csrf.disable())

                // 2. Configuramos los permisos de aduana de red
                .authorizeHttpRequests(auth -> auth
                        // 🎯 Mantenemos la ruta pública para comunicación interna ágil
                        .requestMatchers("/api/ventas/**", "/api/datos/ventas/**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                // 🎯 SOLUCIÓN AL RESETEO ANÓNIMO: Permitimos que las cabeceras custom sigan de largo
                // indicando a Spring que no purgue el contexto de peticiones permitidas de forma pública
                .anonymous(anonymous -> anonymous.disable())

                // 3. Habilitamos Autenticación Básica de respaldo
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("postgres")
                .password("{noop}12345")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
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
                // 1. Deshabilitamos CSRF para poder hacer POST desde Postman
                .csrf(csrf -> csrf.disable())

                // 2. Configuramos los permisos
                .authorizeHttpRequests(auth -> auth
                        // Permitimos la ruta de ventas y la ruta de error (para ver los fallos 500)
                        .requestMatchers("/api/datos/ventas/**", "/error").permitAll()
                        // Cualquier otra cosa requerirá el usuario 'postgres'
                        .anyRequest().authenticated()
                )

                // 3. Habilitamos Autenticación Básica
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // Usuario de prueba igual al de Productos para no confundirse
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("postgres")
                .password("{noop}admin123")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}
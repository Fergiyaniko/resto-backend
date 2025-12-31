package com.example.resto_backend.security.config;

import com.example.resto_backend.security.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/api/v1/auth/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {

                            String uri = request.getRequestURI();

                            if (uri.startsWith("/api")) {
                                writeJsonError(response,
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication is required",
                                        uri,
                                        "Unauthorized");
                            }
                            else {
                                response.sendRedirect("/login");
                            }
                        })
                );

        // Add JWT Authentication filter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeJsonError(HttpServletResponse response, int status, String detail,
                                String instance, String title)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("""
                {
                  "type": "https://api.example.com/problems/authentication-required",
                  "title": "%s",
                  "status": %s,
                  "detail": "%s",
                  "instance": "%s"
                }
            """
            .formatted(title, status, detail, instance)
        );
    }
}



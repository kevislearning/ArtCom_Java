package com.artgallery.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${client.url}")
    private String clientUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Health Check and Error Dispatch
                .requestMatchers("/api/health", "/error").permitAll()
                
                // Auth public routes
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/logout", "/api/auth/google", "/api/auth/artists/recommended", "/api/auth/profile/**", "/api/auth/search").permitAll()
                
                // Illustrations public views
                .requestMatchers(HttpMethod.GET, "/api/illustrations", "/api/illustrations/trending-tags", "/api/illustrations/tags/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/illustrations/{id}").permitAll()
                
                // Comments public views
                .requestMatchers(HttpMethod.GET, "/api/comments/illustration/**").permitAll()
                
                // Static file serving fallback
                .requestMatchers("/uploads/**").permitAll()
                
                // WebSocket handshakes
                .requestMatchers("/ws/**").permitAll()
                
                // Webhook callbacks
                .requestMatchers("/api/wallet/momo-ipn").permitAll()
                
                // Any other request must be authenticated
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> origins = new ArrayList<>();
        if (clientUrl != null && !clientUrl.trim().isEmpty()) {
            for (String url : clientUrl.split(",")) {
                origins.add(url.trim());
            }
        }
        if (!origins.contains("http://localhost:5173")) {
            origins.add("http://localhost:5173");
        }
        
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cookie", "x-requested-with", "accept", "Origin"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

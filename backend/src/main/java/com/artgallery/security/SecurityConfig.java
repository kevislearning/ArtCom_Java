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
                // Cổng kiểm tra sức khỏe hệ thống (Health Check) và điều hướng lỗi
                .requestMatchers("/api/health", "/error").permitAll()
                
                // Các router xác thực (Auth) công khai
                .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/logout", "/api/auth/google", "/api/auth/artists/recommended", "/api/auth/profile/**", "/api/auth/search").permitAll()
                
                // Các view công khai cho Tác phẩm (Illustration)
                .requestMatchers(HttpMethod.GET, "/api/illustrations", "/api/illustrations/trending-tags", "/api/illustrations/tags/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/illustrations/{id}").permitAll()
                
                // Các view công khai cho Bình luận (Comment)
                .requestMatchers(HttpMethod.GET, "/api/comments/illustration/**").permitAll()
                
                // Đường dẫn tĩnh hỗ trợ truy cập file lưu trữ cục bộ
                .requestMatchers("/uploads/**").permitAll()
                
                // Bắt tay kết nối WebSocket
                .requestMatchers("/ws/**").permitAll()
                
                // Điểm nhận callback từ Webhook
                .requestMatchers("/api/wallet/momo-ipn").permitAll()
                
                // Mọi yêu cầu khác đều cần được xác thực
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

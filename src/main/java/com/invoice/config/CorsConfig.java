package com.invoice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("https://invoice-management-5c5t.onrender.com");
        config.addAllowedOrigin("https://invoice-frontend-n9v6.onrender.com");
        config.addAllowedOrigin("http://localhost:3000");  // React dev server
        config.addAllowedOrigin("https://invoice-management.vercel.app"); // production
        config.addAllowedMethod("*");   // GET, POST, PUT, DELETE, PATCH
        config.addAllowedHeader("*");   // all headers including Authorization
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
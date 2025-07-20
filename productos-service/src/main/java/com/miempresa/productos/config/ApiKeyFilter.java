package com.miempresa.productos.config;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class ApiKeyFilter extends OncePerRequestFilter {

    private final String apiKey;

    public ApiKeyFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/api-docs")
            || path.startsWith("/swagger-resources");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestApiKey = request.getHeader("x-api-key");

        if (!apiKey.equals(requestApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"errors\":[{\"status\":\"401\",\"detail\":\"API Key inválida\"}]}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}

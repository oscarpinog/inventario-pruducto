package com.miempresa.inventario.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiKeyFilterConfig {

    @Value("${api.key}")
    private String apiKey;

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilter() {
        FilterRegistrationBean<ApiKeyFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiKeyFilter(apiKey));
        registrationBean.addUrlPatterns("/api/*"); // Protege solo las rutas de tus controladores
        registrationBean.setOrder(1);
        return registrationBean;
    }
}


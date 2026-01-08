package com.accessrequest.config;

import com.accessrequest.api.interceptor.RequestResponseLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for Spring MVC.
 * Registers interceptors and configures web-related settings.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RequestResponseLoggingInterceptor requestResponseLoggingInterceptor;

    /**
     * Register interceptors.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestResponseLoggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/v1/health/**", "/actuator/**");
    }
}

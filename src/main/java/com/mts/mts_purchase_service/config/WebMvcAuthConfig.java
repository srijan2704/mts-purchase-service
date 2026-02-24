package com.mts.mts_purchase_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers auth token interceptor for protected API routes.
 */
@Configuration
public class WebMvcAuthConfig implements WebMvcConfigurer {

    private final AuthTokenInterceptor authTokenInterceptor;

    public WebMvcAuthConfig(AuthTokenInterceptor authTokenInterceptor) {
        this.authTokenInterceptor = authTokenInterceptor;
    }

    /**
     * Applies auth interceptor to API endpoints except public/login/docs routes.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authTokenInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/setup",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}

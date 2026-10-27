package com.backend.core.appconfig;

import com.backend.constant.ApiPathConstants;
import com.backend.core.auth.AuthInterceptor;
import com.backend.core.multitenancy.RequestInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.*;

import java.util.List;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestInterceptor requestInterceptor;

    @Autowired
    private AuthInterceptor authInterceptor;

    @Value("${app.cors.allowed-origins:*}")
    private String[] corsAllowedOrigins;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor).addPathPatterns("/**");
        // Login ke baad wali APIs - naya protected module aaye to uska BASE yaha add karo
        registry.addInterceptor(authInterceptor).addPathPatterns(
                ApiPathConstants.Gym.BASE + "/**",
                ApiPathConstants.Member.BASE + "/**",
                ApiPathConstants.Plan.BASE + "/**",
                ApiPathConstants.Membership.BASE + "/**",
                ApiPathConstants.Dashboard.BASE + "/**");
        WebMvcConfigurer.super.addInterceptors(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedHeaders("*").allowedMethods("*").allowedOrigins(corsAllowedOrigins);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(new ObjectMapper()));
    }
}

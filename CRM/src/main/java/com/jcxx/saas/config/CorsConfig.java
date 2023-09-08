/**
 * Copyright (c) 2010 - 2019  All rights reserved.
 */

package com.jcxx.saas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.parameter}")
    private String parameter;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        String[] split = {"*"};

        if(parameter != null && parameter.length() != 0){
             split = parameter.split(",");
        }
        registry.addMapping("/**")
            .allowedOrigins(split)
            .allowCredentials(true)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .maxAge(3600);
    }
}

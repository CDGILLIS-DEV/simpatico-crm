package com.simpatico.crm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration managing resource view controller mappings and static index handling.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Automatically map /resources/ and /resources to /resources/index.html
        registry.addViewController("/resources/").setViewName("forward:/resources/index.html");
        registry.addViewController("/resources").setViewName("forward:/resources/index.html");
    }
}

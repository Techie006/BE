package com.sparta.cookbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableJpaAuditing
public class CookBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookBankApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowedOrigins("http://localhost:3000", "http://frigo.kr", "https://frigo.kr","https://team7-6nl4perxu-techie006.vercel.app")
                        .exposedHeaders("Authorization","Refresh_Token")
                        .allowCredentials(true);
            }
        };
    }
}

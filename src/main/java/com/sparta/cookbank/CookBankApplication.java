package com.sparta.cookbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling // 스프링 부트에서 스케줄러가 작동하게 합니다.
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
                        .allowedOrigins("http://localhost:3000", "http://frigo.kr", "https://frigo.kr"
                                                                                   ,"https://www.frigo.kr/"
                        ,"https://frigo-three.vercel.app", "https://frigo-techie006.vercel.app"
                        ,"https://frigo-git-vercel-techie006.vercel.app","https://team7-6nl4perxu-techie006.vercel.app")
                        .exposedHeaders("Authorization","Refresh_Token")
                        .allowCredentials(true);
            }
        };
    }
}

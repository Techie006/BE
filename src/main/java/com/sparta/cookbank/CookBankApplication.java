package com.sparta.cookbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CookBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookBankApplication.class, args);
    }

}

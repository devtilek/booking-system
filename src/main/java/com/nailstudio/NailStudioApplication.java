package com.nailstudio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NailStudioApplication {
    public static void main(String[] args) {
        SpringApplication.run(NailStudioApplication.class, args);
    }
}
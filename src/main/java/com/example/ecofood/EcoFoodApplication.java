package com.example.ecofood;

import com.example.ecofood.Util.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EcoFoodApplication {

    public static void main(String[] args) {

        DotenvInitializer.init(); // Load .env
        SpringApplication.run(EcoFoodApplication.class, args);
    }


}

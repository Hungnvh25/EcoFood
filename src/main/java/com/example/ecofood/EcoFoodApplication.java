package com.example.ecofood;

import com.example.ecofood.Util.DotenvInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcoFoodApplication {

    public static void main(String[] args) {

        DotenvInitializer.init(); // Load .env
        SpringApplication.run(EcoFoodApplication.class, args);
    }


}

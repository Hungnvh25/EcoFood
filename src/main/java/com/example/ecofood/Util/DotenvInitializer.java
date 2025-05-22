package com.example.ecofood.Util;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvInitializer {
    public static void init() {
        Dotenv dotenv = Dotenv.load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
}
package com.example.ecofood.controller.test;

import com.example.ecofood.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-gemini")
public class TestGeminiController {

    private final GeminiService geminiService;

    public TestGeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping
    public ResponseEntity<String> testGemini(@RequestParam String input) {
        String response = geminiService.generateText(input);
        return ResponseEntity.ok(response);
    }
}


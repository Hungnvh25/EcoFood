package com.example.ecofood.service;


import com.example.ecofood.domain.Recipe;
import com.example.ecofood.repository.RecipeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeminiWorkerService {

     StringRedisTemplate redisTemplate;


     GeminiService geminiService;

     RecipeRepository recipeRepository;

    private final ThreadPoolExecutor workerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
    public void addToQueue(String input, Long recipeId) {
        String job = String.format("input:%s|recipeId:%d", input, recipeId);
        redisTemplate.opsForList().leftPush("gemini_queue", job);
    }

    @PostConstruct
    public void startWorker() {
        for (int i = 0; i < 3; i++) {
            workerPool.submit(this::processJobs);
        }
    }

    private void processJobs() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String job = (String) redisTemplate.opsForList().rightPop("gemini_queue");

                if (job != null) {
                    String[] parts = job.split("\\|");
                    String input = parts[0].split(":")[1];
                    Long recipeId = Long.valueOf(parts[1].split(":")[1]);

                    String result = geminiService.callGeminiAPI(input);

                    Recipe recipe = recipeRepository.findById(recipeId)
                            .orElseThrow(() -> new RuntimeException("Recipe not found"));

                    recipe.setTextAudio(result);
                    recipeRepository.save(recipe);

                    System.out.println("Hoàn tất xử lý job: " + recipeId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
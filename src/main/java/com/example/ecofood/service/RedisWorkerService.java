    package com.example.ecofood.service;


    import com.example.ecofood.domain.Audio;
    import com.example.ecofood.domain.Recipe;
    import com.example.ecofood.domain.User;
    import com.example.ecofood.domain.UserSetting;
    import com.example.ecofood.repository.RecipeRepository;
    import lombok.AccessLevel;
    import lombok.RequiredArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import org.springframework.data.redis.core.StringRedisTemplate;

    import jakarta.annotation.PostConstruct;
    import org.springframework.stereotype.Service;

    import java.util.concurrent.CompletableFuture;
    import java.util.concurrent.Executors;
    import java.util.concurrent.ThreadPoolExecutor;
    import java.util.concurrent.TimeUnit;

    @Service
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public class RedisWorkerService {

         StringRedisTemplate redisTemplate;
         GeminiService geminiService;
         NotificationService notificationService;
         RecipeRepository recipeRepository;
         TextToSpeechService textToSpeechService;
         AudioService audioService;

        private final ThreadPoolExecutor workerPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(6);
        public void addToQueue(String input, Long recipeId, User.Role role) {
            System.out.println("Nhận yêu cầu xử lý Id: "+ recipeId+" tới Gemini");
            String job = String.format("input;%s|recipeId;%d|role;%s", input, recipeId, role.name());
            redisTemplate.opsForList().leftPush("gemini_queue", job);
        }

        public void addToQueueVietTelAI(String input, Long recipeId) {
            System.out.println("Nhận yêu cầu xử lý Id: "+ recipeId+" tới VietTel AI");
            String job = String.format("input;%s|recipeId;%d", input, recipeId);
            redisTemplate.opsForList().leftPush("viettelAI_queue", job);
        }


        @PostConstruct
        public void startWorker() {
            for (int i = 0; i < 3; i++) {
                workerPool.submit(this::processJobs);
            }
            for (int i = 0; i < 3; i++) {
                workerPool.submit(this::processViettelJobs);
            }
        }


        private void processJobs() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String job = (String) redisTemplate.opsForList().rightPop("gemini_queue", 10, TimeUnit.SECONDS);
                    if (job == null) continue;

                    // Parse job
                    String[] parts = job.split("\\|");
                    String input = parts[0].split(";")[1];
                    Long recipeId = Long.valueOf(parts[1].split(";")[1]);
                    String roleStr = parts[2].split(";")[1];
                    int retry = 0;
                    if (parts.length > 3 && parts[3].startsWith("retry;")) {
                        retry = Integer.parseInt(parts[3].split(";")[1]);
                    }


                    try {
                        CompletableFuture<String> result = geminiService.generateTextAsync(input);
                        Recipe recipe = recipeRepository.findById(recipeId)
                                .orElseThrow(() -> new RuntimeException("Recipe not found"));
                        String textAudio = result.get();
                        recipe.setTextAudio(textAudio);


                        recipeRepository.save(recipe);

                        System.out.println("Đã xử lý yêu cầu Id: "+ recipeId+" tới Gemini");

                        if (roleStr.equals("ADMIN")){
                            recipe.setIsPendingRecipe(false);
                            addToQueueVietTelAI(textAudio, recipe.getId());
                            this.notificationService.createRecipeStatusNotification(recipe, true);
                            recipeRepository.save(recipe);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        // tối đa 3 lần
                        if (retry < 3) {
                            String retryJob = String.format("input;%s|recipeId;%d|role;%s|retry;%d", input, recipeId,roleStr, retry + 1);
                            redisTemplate.opsForList().rightPush("gemini_queue", retryJob);
                        } else {
                            String deadJob = String.format("input;%s|recipeId;%d|role;%s|retry;%d", input, recipeId,roleStr, retry + 1);
                            redisTemplate.opsForList().leftPush("gemini_dead_jobs", deadJob);
                            System.err.println("Job chết, chuyển vào dead_jobs; " + deadJob);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }


        private void processViettelJobs() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String job = (String) redisTemplate.opsForList().rightPop("viettelAI_queue", 10, TimeUnit.SECONDS);
                    if (job == null) continue;

                    // Parse job
                    String[] parts = job.split("\\|");
                    String input = parts[0].split(";")[1];
                    Long recipeId = Long.valueOf(parts[1].split(";")[1]);
                    int retry = 0;
                    if (parts.length > 2 && parts[2].startsWith("retry;")) {
                        retry = Integer.parseInt(parts[2].split(";")[1]);
                    }

                    try {
                        Recipe recipe = recipeRepository.findById(recipeId)
                                .orElseThrow(() -> new RuntimeException("Recipe not found"));
                        User user = recipe.getUser();
                        String voice = String.valueOf(user.getUserSetting().getAccent());
                        float speed = 1.0F; // tùy chỉnh

                        String urlAudio = textToSpeechService.convertTextToSpeech(input, voice, speed);
                        if (urlAudio == null || urlAudio.isEmpty()) throw new RuntimeException("Không lấy được url audio từ Viettel");

                        Audio audio = Audio.builder()
                                .accent(UserSetting.Accent.fromValue(voice))
                                .urlAudio(urlAudio)
                                .voiceGender(user.getUserSetting().getVoiceGender())
                                .recipe(recipe)
                                .build();
                        audioService.saveAudio(audio);
                        recipe.setIsPendingRecipe(false);
                        recipeRepository.save(recipe);
                        System.out.println("Đã xử lý yêu cầu Id: "+ recipeId+" tới VietTel AI");
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (retry < 3) {
                            String retryJob = String.format("input;%s|recipeId;%d|retry;%d", input, recipeId, retry + 1);
                            redisTemplate.opsForList().rightPush("viettelAI_queue", retryJob);
                        } else {
                            String deadJob = String.format("input;%s|recipeId;%d|retry;%d", input, recipeId, retry + 1);
                            redisTemplate.opsForList().leftPush("viettelAI_dead_jobs", deadJob);
                            System.err.println("ViettelAI Job chết, chuyển vào dead_jobs; " + deadJob);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }



    }
package com.example.ecofood.service;

import com.example.ecofood.DTO.TTSObj;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class TextToSpeechService {

    private final OkHttpClient client = new OkHttpClient();

    @Value("${viettel.api.key}")
    private String TOKEN;

    @Async
    public CompletableFuture<String> convertTextToSpeechAsync(String text, String voiceCode, float speed) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return convertTextToSpeech(text, voiceCode, speed);
            } catch (IOException e) {
                throw new RuntimeException("Failed to convert text to speech", e);
            }
        });
    }

    public String convertTextToSpeech(String text, String voiceCode, float speed) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        TTSObj ttsObj = TTSObj.builder()
                .text(text)
                .voice(voiceCode)
                .speed(speed)
                .tts_return_option(3)
                .token(TOKEN)
                .without_filter("false")
                .build();

        String json = objectMapper.writeValueAsString(ttsObj);
        RequestBody body = RequestBody.create(mediaType, json);

        Request request = new Request.Builder()
                .url("https://viettelai.vn/tts/speech_synthesis")
                .method("POST", body)
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException("API call failed: " + response.code() + " - " + response.message());
            }

            byte[] audioBytes = response.body().bytes();

            String fileName = UUID.randomUUID().toString() + ".mp3";
            String absolutePathSrc = Paths.get("src/main/resources/static/audio/", fileName).toString();
            String absolutePathTarget = Paths.get("target/classes/static/audio/", fileName).toString();

            File dirSrc = new File("src/main/resources/static/audio/");
            if (!dirSrc.exists()) dirSrc.mkdirs();

            File dirTarget = new File("target/classes/static/audio/");
            if (!dirTarget.exists()) dirTarget.mkdirs();

            try (FileOutputStream fos = new FileOutputStream(absolutePathSrc)) {
                fos.write(audioBytes);
            }

            try (FileOutputStream fos = new FileOutputStream(absolutePathTarget)) {
                fos.write(audioBytes);
            }

            return "/audio/" + fileName;
        }
    }
}


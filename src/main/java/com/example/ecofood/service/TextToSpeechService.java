package com.example.ecofood.service;

import com.example.ecofood.DTO.TTSObj;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TextToSpeechService {

    private final OkHttpClient client = new OkHttpClient();

    @Value("${viettel.api.key}")
    private String TOKEN;

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

            // Tạo tên file ngẫu nhiên
            String fileName = UUID.randomUUID().toString() + ".mp3";

            // Đường dẫn lưu file
            String absolutePathSrc = Paths.get("src/main/resources/static/audio/", fileName).toString();
            String absolutePathTarget = Paths.get("target/classes/static/audio/", fileName).toString();

            // Tạo thư mục nếu chưa tồn tại
            File dirSrc = new File("src/main/resources/static/audio/");
            if (!dirSrc.exists()) {
                dirSrc.mkdirs();
            }

            File dirTarget = new File("target/classes/static/audio/");
            if (!dirTarget.exists()) {
                dirTarget.mkdirs();
            }

            // Ghi file vào src/main/resources
            try (FileOutputStream fos = new FileOutputStream(absolutePathSrc)) {
                fos.write(audioBytes);
            }

            // Ghi file vào target/classes
            try (FileOutputStream fos = new FileOutputStream(absolutePathTarget)) {
                fos.write(audioBytes);
            }

            // Trả về đường dẫn tương đối client có thể truy cập (Spring Boot phục vụ static ở /static/**)
            return "/audio/" + fileName;
        }
    }
}

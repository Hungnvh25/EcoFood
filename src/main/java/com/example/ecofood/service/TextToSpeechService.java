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
    private  String TOKEN ;

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

            // Đường dẫn đến thư mục static/audio
            String relativePath = "static/audio/";
            String absolutePath = Paths.get("src/main/resources", relativePath, fileName).toString();

            // Đảm bảo thư mục tồn tại
            File dir = new File("src/main/resources/" + relativePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Ghi file ra đĩa
            try (FileOutputStream fos = new FileOutputStream(absolutePath)) {
                fos.write(audioBytes);
            }

            // Trả về đường dẫn tương đối mà client có thể truy cập được (Spring Boot phục vụ static ở /static/**)
            return "/audio/" + fileName;
        }
    }
}

package com.example.ecofood.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ImageService {
    static final String UPLOAD_DIR = "src/main/resources/static";
    static final String UPLOAD_TARGET = "target/classes/static";

    public String saveImage(MultipartFile profileImage, String photoFolder) throws IOException {


        if (!profileImage.isEmpty()) {
            String fileName = "/" + photoFolder + "/" + System.currentTimeMillis() + "_" + profileImage.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, profileImage.getBytes());


//            lưu ngay vào thư mục target
            Path filePath_Target = Paths.get(UPLOAD_TARGET + fileName);
            Files.createDirectories(filePath_Target.getParent());
            Files.write(filePath_Target, profileImage.getBytes());

            return fileName;
        }
        return null;
    }
}

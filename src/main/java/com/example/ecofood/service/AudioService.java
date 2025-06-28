package com.example.ecofood.service;

import com.example.ecofood.domain.Audio;
import com.example.ecofood.repository.AudioRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AudioService {
    AudioRepository audioRepository;

    public void saveAudio(Audio audio){
        this.audioRepository.save(audio);
    }

}

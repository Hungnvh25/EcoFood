package com.example.ecofood.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TTSObj {

    String text;
    String voice;
    Float speed;
    Integer tts_return_option;
    String token;
    String without_filter;

}

package com.example.ecofood.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Audio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;


    @Enumerated(EnumType.STRING)
    UserSetting.Gender voiceGender;

    @Enumerated(EnumType.STRING)
    UserSetting.Accent accent;

    String urlAudio;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    public enum Gender {
        MALE,
        FEMALE
    }

    public enum Accent {
        HN_QUYNHANH("hn-quynhanh"),
        HCM_DIEMMY("hcm-diemmy"),
        HUE_MAINGOC("hue-maingoc"),
        HN_PHUONGTRANG("hn-phuongtrang"),
        HN_THAOCHI("hn-thaochi"),
        HN_THANHHA("hn-thanhha"),
        HCM_PHUONGLY("hcm-phuongly"),
        HCM_THUYDUNG("hcm-thuydung"),
        HN_THANHTUNG("hn-thanhtung"),
        HUE_BAOQUOC("hue-baoquoc"),
        HCM_MINHQUAN("hcm-minhquan"),
        HN_THANHPHUONG("hn-thanhphuong"),
        HN_NAMKHANH("hn-namkhanh"),
        HN_LEYEN("hn-leyen"),
        HN_TIENQUAN("hn-tienquan"),
        HCM_THUYDUYEN("hcm-thuyduyen");

        private final String value;

        Accent(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}

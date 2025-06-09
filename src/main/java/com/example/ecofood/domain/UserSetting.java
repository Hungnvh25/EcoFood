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
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    Gender voiceGender ;

    @Enumerated(EnumType.STRING)
    Accent accent;

    @Enumerated(EnumType.STRING)
    Category.Region region;

    String urlImage;


    @OneToOne
    @JoinColumn(name = "user_id",referencedColumnName = "id")
    private User user;


    public enum Gender {
        MALE,
        FEMALE
    }


    @Getter
    public enum Region {
        NORTH("Miền Bắc"),
        CENTRAL("Miền Trung"),
        SOUTH("Miền Nam");

        private String description;

        Region(String description) {
            this.description = description;
        }

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

        public static Accent fromValue(String value) {
            for (Accent accent : values()) {
                if (accent.value.equalsIgnoreCase(value)) {
                    return accent;
                }
            }
            throw new IllegalArgumentException("Unknown accent code: " + value);
        }
    }


}
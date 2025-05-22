package com.example.ecofood.Util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class RecipeUtils {

    public  String normalizeRecipeName(String name) {
        if (name == null) return "";

        String normalized = name.trim().replaceAll("\\s+", " ");
        normalized = normalized.toLowerCase(); // chư thường
        normalized = removeVietnameseDiacritics(normalized); // bỏ dấu
        normalized = normalized.replaceAll("[^a-z0-9 ]", ""); //Xóa ký tự đặc biệt
        return normalized;
    }

    // Hàm loại bỏ dấu tiếng Việt
    public  String removeVietnameseDiacritics(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

}

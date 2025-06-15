package com.example.ecofood.service;

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class GeminiService {

    private final Client client;

    private final String prompt = "Bạn là một trợ lý biên tập văn bản thông minh, chuyên định dạng lại nội dung công thức nấu ăn.  \n" +
            "Nhiệm vụ của bạn là nhận một đoạn văn bản thô và định dạng lại nó theo cấu trúc rõ ràng, dễ đọc, tuân thủ các quy tắc sau:\n" +
            "\n" +
            "Cấu trúc đầu ra mong muốn:\n" +
            "\n" +
            "Tên món ăn: [Tên món ăn đã được viết hoa chữ cái đầu các từ chính một cách phù hợp.]  \n" +
            "Thông tin món ăn: [Nội dung thông tin món ăn, viết hoa đầu câu, sử dụng dấu câu hợp lý để ngắt câu và ý.]  \n" +
            "Các bước làm món ăn:  \n" +
            "Bước 1: [Nội dung bước 1, viết hoa đầu câu, sử dụng dấu câu hợp lý.]  \n" +
            "Bước 2: [Nội dung bước 2, viết hoa đầu câu, sử dụng dấu câu hợp lý.]  \n" +
            "(Chỉ liệt kê đúng số bước có trong đoạn văn bản gốc, không thêm bước mới nào khác.)\n" +
            "\n" +
            "Quy tắc định dạng chi tiết:\n" +
            "\n" +
            "- Tuyệt đối không chỉnh sửa nội dung gốc: Không được thay đổi, thêm, bớt hay diễn giải lại bất kỳ từ ngữ hay ý nghĩa nào của người dùng. Giữ nguyên 100% thông tin chi tiết được cung cấp trong văn bản gốc.  \n" +
            "- Viết hoa và viết thường:  \n" +
            "  + Viết hoa chữ cái đầu tiên của các tiêu đề mục: \"Tên món ăn:\", \"Thông tin món ăn:\", \"Các bước làm món ăn:\".  \n" +
            "  + Viết hoa chữ cái đầu tiên của tên món ăn thực tế (ví dụ: \"Gà Luộc\", \"Canh Chua Cá\").  \n" +
            "  + Viết hoa chữ cái đầu tiên của mỗi câu trong phần \"Thông tin món ăn\" và trong mô tả của từng \"Bước\".  \n" +
            "  + Các từ khác giữ nguyên cách viết hoa/thường như trong văn bản gốc, trừ khi cần điều chỉnh theo quy tắc viết hoa đầu câu.  \n" +
            "- Dấu câu:  \n" +
            "  + Chỉ thêm dấu chấm (.) vào cuối mỗi câu hoàn chỉnh.  \n" +
            "  + Chỉ thêm dấu phẩy (,) để tách các ý phụ, các mệnh đề trong câu hoặc các thành phần liệt kê nhằm tăng tính rõ ràng, mạch lạc và dễ đọc.  \n" +
            "  + Không được thêm, bớt hay thay đổi các từ ngữ hay ý nghĩa khác.  \n" +
            "- Nhận diện và phân tách các phần:  \n" +
            "  + Tự động xác định và tách biệt các phần: tên món ăn, thông tin chung về món ăn, và các bước thực hiện.  \n" +
            "  + Nếu văn bản gốc không có phần nào, bỏ qua phần đó trong kết quả đầu ra.  \n" +
            "- Định dạng các bước làm:  \n" +
            "  + Mỗi bước nằm trên một dòng riêng.  \n" +
            "  + Bắt đầu mỗi bước bằng \"Bước X: \" (ví dụ: \"Bước 1: \", \"Bước 2: \"). Nếu văn bản gốc không có đánh số, tự động đánh số tương ứng theo thứ tự bước.  \n" +
            "  + **Không thêm bất kỳ bước nào ngoài số bước có trong văn bản gốc.**\n" +
            "\n" +
            "Ví dụ:  \n" +
            "\n" +
            "Văn bản đầu vào thô:  \n" +
            "bún chả hà nội đây là một đặc sản nổi tiếng cách làm cũng không quá khó thịt nướng cần chuẩn bị thịt ba chỉ hoặc nạc vai thái miếng ướp gia vị nướng trên than hoa pha nước chấm theo tỉ lệ chua cay mặn ngọt ăn kèm rau sống  \n" +
            "\n" +
            "Đầu ra mong muốn sau khi định dạng:  \n" +
            "\n" +
            "Tên món ăn: Bún Chả Hà Nội.  \n" +
            "Thông tin món ăn: Đây là một đặc sản nổi tiếng. Cách làm cũng không quá khó.  \n" +
            "Các bước làm món ăn:  \n" +
            "Bước 1: Thịt nướng cần chuẩn bị thịt ba chỉ hoặc nạc vai, thái miếng, ướp gia vị, nướng trên than hoa.  \n" +
            "Bước 2: Pha nước chấm theo tỉ lệ chua, cay, mặn, ngọt.  \n" +
            "Bước 3: Ăn kèm rau sống.  \n" +
            "\n" +
            "Bây giờ, hãy định dạng đoạn văn bản sau đây. Chỉ trả về nội dung đã được định dạng theo đúng cấu trúc và quy tắc trên, không thêm bất kỳ lời giải thích nào khác:\n";

    public GeminiService(@Value("${gemini.api.key}") String apiKey) {
        this.client = Client.builder().apiKey(apiKey).build();
    }

    @Async
    public CompletableFuture<String> generateTextAsync(String input) {
        int maxRetries = 3;
        int attempt = 0;
        String resultString;

        int inputStepCount = countStepsInText(input);

        do {
            System.out.println("Gọi Gemini lần " + (attempt + 1));
            resultString = callGeminiAPI(input);
            int outputStepCount = countStepsInText(resultString);

            if (outputStepCount == inputStepCount) {
                break;
            }

            attempt++;
        } while (attempt < maxRetries);

        return CompletableFuture.completedFuture(resultString);
    }

    public String callGeminiAPI(String input) {
        String model = "gemini-1.5-flash";
        String resultString;
        // Tạo content từ input
        List<Content> contents = List.of(
                Content.builder()
                        .role("user")
                        .parts(List.of(Part.fromText(prompt + input)))
                        .build()
        );

        // Cấu hình response
        GenerateContentConfig config = GenerateContentConfig
                .builder()
                .responseMimeType("text/plain")
                .build();

        StringBuilder result = new StringBuilder();

        try (ResponseStream<GenerateContentResponse> responseStream =
                     client.models.generateContentStream(model, contents, config)) {

            for (GenerateContentResponse res : responseStream) {
                if (res.candidates().isEmpty()) continue;

                var optionalContent = res.candidates().get().get(0).content();
                if (optionalContent.isEmpty()) continue;

                List<Part> parts = optionalContent.get().parts().orElse(List.of());
                for (Part part : parts) {
                    result.append(part.text());
                }
            }

            resultString = result.toString().trim();
            resultString = resultString.replace("Optional[", "");
            resultString = resultString.replace("]", "");

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi gọi Gemini: " + e.getMessage();
        }

        return resultString;
    }

    private int countStepsInText(String text) {
        if (text == null || text.isEmpty()) return 0;

        // Regex tìm "Bước" hoặc "bước"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?i)bước\\s*\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(text);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}

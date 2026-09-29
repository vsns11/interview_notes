package ch05.files;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    public record UploadResult(String fileName, long bytes, String contentType, int lines, String note) {}

    // POST /api/uploads  (multipart/form-data with a part named "file" and a field "note")
    @PostMapping("/api/uploads")
    public UploadResult upload(@RequestPart("file") MultipartFile file,
                               @RequestParam(defaultValue = "") String note) throws IOException {
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        return new UploadResult(file.getOriginalFilename(), file.getSize(),
                file.getContentType(), (int) text.lines().count(), note);
    }
}

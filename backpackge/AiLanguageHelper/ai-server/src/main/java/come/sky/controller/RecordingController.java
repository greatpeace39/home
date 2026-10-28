package come.sky.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/recordings")
public class RecordingController {

    private final Path uploadRoot;

    public RecordingController(
            @Value("${app.recordings.upload-dir:recordings}") String uploadDir
    ) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadRecording(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>("Recording is empty", HttpStatus.BAD_REQUEST);
        }

        try {
            Files.createDirectories(uploadRoot);
            String filename = UUID.randomUUID().toString() + safeExtension(file.getOriginalFilename());
            Path target = uploadRoot.resolve(filename).normalize();
            if (!target.startsWith(uploadRoot)) {
                return new ResponseEntity<>("Invalid filename", HttpStatus.BAD_REQUEST);
            }

            // The client filename never becomes a storage path. Do not overwrite existing files.
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target);
            }
            return new ResponseEntity<>("Recording uploaded: " + filename, HttpStatus.OK);
        } catch (IOException exception) {
            return new ResponseEntity<>("Recording upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String safeExtension(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String extension = originalFilename.substring(dot + 1);
        return extension.matches("[A-Za-z0-9]{1,10}") ? "." + extension : "";
    }
}

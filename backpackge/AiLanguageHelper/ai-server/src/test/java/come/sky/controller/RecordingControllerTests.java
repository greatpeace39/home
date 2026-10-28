package come.sky.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

class RecordingControllerTests {

    @TempDir
    Path directory;

    @Test
    void traversalFilenameCannotOverwriteOutsideFile() throws IOException {
        Path outside = directory.resolve("outside.wav");
        Files.writeString(outside, "original");
        Path uploadRoot = directory.resolve("recordings");
        RecordingController controller = new RecordingController(uploadRoot.toString());

        ResponseEntity<String> response = controller.uploadRecording(file("../outside.wav", "new"));

        assertEquals(200, response.getStatusCode().value());
        assertEquals("original", Files.readString(outside));
        assertEquals(1, countFiles(uploadRoot));
        assertFalse(response.getBody().contains(directory.toString()));
    }

    @Test
    void repeatedClientFilenameCreatesDifferentFiles() throws IOException {
        RecordingController controller = new RecordingController(directory.toString());

        assertEquals(200, controller.uploadRecording(file("same.wav", "one")).getStatusCode().value());
        assertEquals(200, controller.uploadRecording(file("same.wav", "two")).getStatusCode().value());

        assertEquals(2, countFiles(directory));
        assertFalse(Files.exists(directory.resolve("same.wav")));
    }

    @Test
    void windowsAndAbsoluteNamesNeverChooseTheStoragePath() throws IOException {
        RecordingController controller = new RecordingController(directory.toString());
        for (String filename : new String[] {
                "..\\outside.wav", "C:\\Windows\\outside.wav", "/tmp/outside.wav", "..", "audio.wav/../outside"
        }) {
            assertEquals(200, controller.uploadRecording(file(filename, "audio")).getStatusCode().value());
        }
        assertEquals(5, countFiles(directory));
        try (Stream<Path> files = Files.list(directory)) {
            assertTrue(files.allMatch(path -> path.getParent().equals(directory)));
        }
    }

    @Test
    void emptyFileIsRejected() {
        RecordingController controller = new RecordingController(directory.toString());

        assertEquals(400, controller.uploadRecording(file("empty.wav", "")).getStatusCode().value());
    }

    private MockMultipartFile file(String filename, String value) {
        return new MockMultipartFile("file", filename, "audio/wav", value.getBytes(StandardCharsets.UTF_8));
    }

    private long countFiles(Path path) throws IOException {
        try (Stream<Path> files = Files.list(path)) {
            return files.count();
        }
    }
}

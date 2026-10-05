package com.example.ai_coding_assistent.indexing;

import com.example.ai_coding_assistent.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileFilterServiceTest {

    private FileFilterService fileFilterService;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        fileFilterService = new FileFilterService(appProperties);
    }

    @Test
    void shouldAcceptSupportedSourceFiles(@TempDir Path tempDir) throws IOException {
        Path javaFile = tempDir.resolve("UserService.java");
        Files.writeString(javaFile, "public class UserService {}");

        Path tsFile = tempDir.resolve("App.tsx");
        Files.writeString(tsFile, "export const App = () => null;");

        Path pyFile = tempDir.resolve("main.py");
        Files.writeString(pyFile, "print('hello')");

        assertTrue(fileFilterService.shouldIndex(tempDir, javaFile));
        assertTrue(fileFilterService.shouldIndex(tempDir, tsFile));
        assertTrue(fileFilterService.shouldIndex(tempDir, pyFile));
    }

    @Test
    void shouldRejectIgnoredDirectoriesAndFiles(@TempDir Path tempDir) throws IOException {
        Path nodeModules = tempDir.resolve("node_modules");
        Files.createDirectories(nodeModules);
        Path nmFile = nodeModules.resolve("package.json");
        Files.writeString(nmFile, "{}");

        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(targetDir);
        Path targetFile = targetDir.resolve("App.class");
        Files.writeString(targetFile, "binary");

        Path lockFile = tempDir.resolve("package-lock.json");
        Files.writeString(lockFile, "{}");

        assertFalse(fileFilterService.shouldIndex(tempDir, nmFile));
        assertFalse(fileFilterService.shouldIndex(tempDir, targetFile));
        assertFalse(fileFilterService.shouldIndex(tempDir, lockFile));
    }

    @Test
    void shouldDetectLanguagesCorrectly() {
        assertEquals("java", fileFilterService.detectLanguage("OrderController.java"));
        assertEquals("typescript", fileFilterService.detectLanguage("index.ts"));
        assertEquals("typescript", fileFilterService.detectLanguage("Component.tsx"));
        assertEquals("python", fileFilterService.detectLanguage("script.py"));
        assertEquals("markdown", fileFilterService.detectLanguage("README.md"));
        assertEquals("yaml", fileFilterService.detectLanguage("application.yml"));
    }
}
